package controlador;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import modelo.persona;
import modelo.personaDAO;
import modelo.JsonContactoService;
import vista.ventana;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.Locale;
import java.util.ResourceBundle;

public class logica_ventana implements ActionListener {

    private final ventana delegado;
    private final personaDAO dao;
    private List<persona> contactos;
    private TableRowSorter<?> sorter;
    private ResourceBundle textos;
    private SwingWorker<?, ?> workerBusqueda;

    public logica_ventana(ventana delegado) {
        this.delegado = delegado;
        this.dao = new personaDAO();
        this.contactos = new ArrayList<>();

        // Cargar idioma por defecto (español)
        cargarIdioma(new Locale("es", "ES"));
        configurarEventos();
        configurarTablaYFiltro();
        configurarAtajosTeclado();
        configurarMenuContextual();
        procesarArchivoEnSegundoPlano(null, false); // carga inicial de contactos
    }

    // ================================
    // CONFIGURACIÓN DE EVENTOS
    // ================================
    private void configurarEventos() {
        delegado.btn_add.addActionListener(this);
        delegado.btn_modificar.addActionListener(this);
        delegado.btn_eliminar.addActionListener(this);
        delegado.btn_importarJson.addActionListener(this);
        delegado.btn_exportarJson.addActionListener(this);
        delegado.btnToggleTheme.addActionListener(e -> toggleTheme());

        delegado.tbl_contactos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) cargarFilaEnFormulario();
        });

        // Búsqueda con filtro en JTable (con retardo y SwingWorker)
        delegado.txt_buscar.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { ejecutarFiltro(); }
            @Override public void removeUpdate(DocumentEvent e) { ejecutarFiltro(); }
            @Override public void changedUpdate(DocumentEvent e) { ejecutarFiltro(); }
            private void ejecutarFiltro() {
                if (workerBusqueda != null && !workerBusqueda.isDone())
                    workerBusqueda.cancel(true);
                workerBusqueda = new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        Thread.sleep(300);
                        if (isCancelled()) return null;
                        String texto = delegado.txt_buscar.getText().trim();
                        RowFilter<Object, Object> filtro = null;
                        if (!texto.isEmpty())
                            filtro = RowFilter.regexFilter("(?i)" + Pattern.quote(texto));
                        final RowFilter<Object, Object> filtroFinal = filtro;
                        SwingUtilities.invokeLater(() -> sorter.setRowFilter(filtroFinal));
                        return null;
                    }
                };
                workerBusqueda.execute();
            }
        });

        // Cambio de idioma desde el comboBox
        delegado.cmb_idiomas.addActionListener(e -> {
            int seleccion = delegado.cmb_idiomas.getSelectedIndex();
            switch (seleccion) {
                case 0: cargarIdioma(new Locale("es", "ES")); break;
                case 1: cargarIdioma(new Locale("en", "US")); break;
                case 2: cargarIdioma(new Locale("pt", "BR")); break;
            }
        });
    }

    /**
     * Carga los textos internacionalizados desde los archivos .properties
     * que están en src/main/resources/recursos/mensajes_*.properties
     */
    private void cargarIdioma(Locale locale) {
        try {
            // IMPORTANTE: "recursos.mensajes" porque los .properties están dentro de la carpeta "recursos"
            textos = ResourceBundle.getBundle("recursos.mensajes", locale);
            delegado.aplicarIdioma(textos);
            actualizarEstadisticas();
        } catch (Exception e) {
            System.out.println("Error al cargar idioma " + locale + ": " + e.getMessage());
        }
    }

    private void configurarTablaYFiltro() {
        sorter = new TableRowSorter<>(delegado.tableModel);
        delegado.tbl_contactos.setRowSorter(sorter);
    }

    // ================================
    // ATAJOS DE TECLADO Y MENÚ CONTEXTUAL
    // ================================
    private void configurarAtajosTeclado() {
        InputMap im = delegado.contentPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = delegado.contentPane.getActionMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK), "nuevo");
        am.put("nuevo", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { limpiarCampos(); } });
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK), "exportar");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "eliminar");
        am.put("eliminar", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { eliminarSeleccionado(); } });
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK), "buscar");
        am.put("buscar", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { delegado.txt_buscar.requestFocus(); } });
    }

    private void configurarMenuContextual() {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem editar = new JMenuItem("Editar");
        JMenuItem eliminar = new JMenuItem("Eliminar");
        editar.addActionListener(e -> cargarFilaEnFormulario());
        eliminar.addActionListener(e -> eliminarSeleccionado());
        menu.add(editar);
        menu.add(eliminar);
        delegado.tbl_contactos.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { mostrarPopup(e); }
            @Override public void mouseReleased(MouseEvent e) { mostrarPopup(e); }
            private void mostrarPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int row = delegado.tbl_contactos.rowAtPoint(e.getPoint());
                    if (row >= 0) delegado.tbl_contactos.setRowSelectionInterval(row, row);
                    menu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }

    // ================================
    // CARGA INICIAL
    // ================================
    private void procesarArchivoEnSegundoPlano(File archivoImportar, boolean esImportacion) {
        String msg = (textos != null)
                ? textos.getString(esImportacion ? "msg.procesando" : "msg.cargando")
                : "Cargando...";
        delegado.progressBar.setString(msg);
        delegado.progressBar.setIndeterminate(true);

        SwingWorker<List<persona>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<persona> doInBackground() throws Exception {
                Thread.sleep(600); // simula animación
                if (esImportacion && archivoImportar != null)
                    return dao.leerCualquierArchivo(archivoImportar);
                else
                    return dao.leerArchivo();
            }

            @Override
            protected void done() {
                delegado.progressBar.setIndeterminate(false);
                delegado.progressBar.setValue(100);
                try {
                    List<persona> datos = get();
                    if (esImportacion) {
                        contactos.addAll(datos);
                        dao.guardarTodos(contactos);
                        JOptionPane.showMessageDialog(delegado,
                                textos.getString("msg.import.exito") + datos.size());
                    } else {
                        contactos = datos;
                    }
                    refrescarTabla();
                    actualizarEstadisticas();
                    delegado.progressBar.setString("100%");
                } catch (Exception e) {
                    delegado.progressBar.setString("Error");
                    JOptionPane.showMessageDialog(delegado, textos.getString("msg.error.csv"));
                }
            }
        };
        worker.execute();
    }

    // ================================
    // OPERACIONES CON JSON
    // ================================
    private void importarJSON() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(textos.getString("msg.titulo.importar.json"));
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("JSON (*.json)", "json"));
        if (chooser.showOpenDialog(delegado) != JFileChooser.APPROVE_OPTION) return;
        File archivo = chooser.getSelectedFile();

        delegado.progressBar.setString(textos.getString("msg.procesando.json"));
        delegado.progressBar.setIndeterminate(true);

        SwingWorker<List<persona>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<persona> doInBackground() throws Exception {
                return new JsonContactoService().importarContactos(archivo);
            }
            @Override
            protected void done() {
                delegado.progressBar.setIndeterminate(false);
                try {
                    List<persona> importados = get();
                    synchronized (contactos) {
                        contactos.addAll(importados);
                        dao.guardarTodos(contactos);
                    }
                    refrescarTabla();
                    actualizarEstadisticas();
                    delegado.progressBar.setString("100%");
                    JOptionPane.showMessageDialog(delegado,
                            textos.getString("msg.import.json.exito") + importados.size());
                } catch (Exception e) {
                    delegado.progressBar.setString("Error");
                    JOptionPane.showMessageDialog(delegado,
                            textos.getString("msg.error.json") + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void exportarJSON() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(textos.getString("msg.titulo.exportar.json"));
        chooser.setSelectedFile(new File("contactos.json"));
        if (chooser.showSaveDialog(delegado) != JFileChooser.APPROVE_OPTION) return;
        File archivo = chooser.getSelectedFile();
        if (!archivo.getName().endsWith(".json"))
            archivo = new File(archivo.getAbsolutePath() + ".json");
        final File destino = archivo;

        delegado.progressBar.setString(textos.getString("msg.exportando.json"));
        delegado.progressBar.setIndeterminate(true);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                synchronized (contactos) {
                    new JsonContactoService().exportarContactos(contactos, destino);
                }
                return null;
            }
            @Override
            protected void done() {
                delegado.progressBar.setIndeterminate(false);
                delegado.progressBar.setString("100%");
                JOptionPane.showMessageDialog(delegado,
                        textos.getString("msg.export.json.exito") + destino.getAbsolutePath());
            }
        };
        worker.execute();
    }

    // ================================
    // MÉTODOS AUXILIARES (CRUD, TABLA, FORMULARIO)
    // ================================
    private void refrescarTabla() {
        delegado.tableModel.setRowCount(0);
        for (persona p : contactos) {
            delegado.tableModel.addRow(new Object[]{
                    p.getNombre(), p.getTelefono(), p.getEmail(), p.getCategoria(), p.isFavorito()
            });
        }
    }

    private boolean validarCampos() {
        if (delegado.txt_nombres.getText().trim().isEmpty() ||
                delegado.txt_telefono.getText().trim().isEmpty() ||
                delegado.txt_email.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(delegado, textos.getString("msg.completa.campos"));
            return false;
        }
        return true;
    }

    private persona construirDesdeFormulario() {
        return new persona(
                delegado.txt_nombres.getText().trim(),
                delegado.txt_telefono.getText().trim(),
                delegado.txt_email.getText().trim(),
                delegado.cmb_categoria.getSelectedItem().toString(),
                delegado.chb_favorito.isSelected()
        );
    }

    private void limpiarCampos() {
        delegado.txt_nombres.setText("");
        delegado.txt_telefono.setText("");
        delegado.txt_email.setText("");
        delegado.cmb_categoria.setSelectedIndex(0);
        delegado.chb_favorito.setSelected(false);
        delegado.tbl_contactos.clearSelection();
        delegado.txt_nombres.requestFocus();
    }

    private void cargarFilaEnFormulario() {
        int rowView = delegado.tbl_contactos.getSelectedRow();
        if (rowView < 0) return;
        int rowModel = delegado.tbl_contactos.convertRowIndexToModel(rowView);
        delegado.txt_nombres.setText(delegado.tableModel.getValueAt(rowModel, 0).toString());
        delegado.txt_telefono.setText(delegado.tableModel.getValueAt(rowModel, 1).toString());
        delegado.txt_email.setText(delegado.tableModel.getValueAt(rowModel, 2).toString());
        delegado.cmb_categoria.setSelectedItem(delegado.tableModel.getValueAt(rowModel, 3).toString());
        delegado.chb_favorito.setSelected(Boolean.parseBoolean(delegado.tableModel.getValueAt(rowModel, 4).toString()));
    }

    // CRUD con sincronización básica
    private void agregarContacto() {
        if (!validarCampos()) return;
        persona nuevo = construirDesdeFormulario();

        // Validación en hilo separado para no bloquear UI
        new Thread(() -> {
            boolean existe;
            synchronized (contactos) {
                existe = contactos.stream().anyMatch(p ->
                        p.getNombre().equalsIgnoreCase(nuevo.getNombre()) ||
                                p.getTelefono().equals(nuevo.getTelefono()) ||
                                p.getEmail().equalsIgnoreCase(nuevo.getEmail()));
            }
            final boolean duplicado = existe;
            SwingUtilities.invokeLater(() -> {
                if (duplicado) {
                    mostrarNotificacionTemporal("Contacto duplicado", false);
                } else {
                    contactos.add(nuevo);
                    if (dao.guardarTodos(contactos)) {
                        refrescarTabla();
                        actualizarEstadisticas();
                        limpiarCampos();
                        mostrarNotificacionTemporal(textos.getString("msg.contacto.agregado"), true);
                    } else {
                        mostrarNotificacionTemporal(textos.getString("msg.error.guardar"), false);
                    }
                }
            });
        }).start();
    }

    private void modificarContacto() {
        int rowView = delegado.tbl_contactos.getSelectedRow();
        if (rowView < 0) {
            mostrarNotificacionTemporal(textos.getString("msg.selecciona.modificar"), false);
            return;
        }
        if (!validarCampos()) return;

        int rowModel = delegado.tbl_contactos.convertRowIndexToModel(rowView);
        persona nuevo = construirDesdeFormulario();
        synchronized (contactos) {
            contactos.set(rowModel, nuevo);
            if (dao.guardarTodos(contactos)) {
                refrescarTabla();
                actualizarEstadisticas();
                mostrarNotificacionTemporal(textos.getString("msg.contacto.modificado"), true);
            } else {
                mostrarNotificacionTemporal(textos.getString("msg.error.modificar"), false);
            }
        }
    }

    private void eliminarSeleccionado() {
        int rowView = delegado.tbl_contactos.getSelectedRow();
        if (rowView < 0) {
            JOptionPane.showMessageDialog(delegado, textos.getString("msg.selecciona.eliminar"));
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(delegado,
                textos.getString("msg.confirmar.eliminar"),
                textos.getString("msg.titulo.confirmar"),
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        int rowModel = delegado.tbl_contactos.convertRowIndexToModel(rowView);
        contactos.remove(rowModel);
        if (dao.guardarTodos(contactos)) {
            refrescarTabla();
            actualizarEstadisticas();
            limpiarCampos();
            JOptionPane.showMessageDialog(delegado, textos.getString("msg.contacto.eliminado"));
        } else {
            JOptionPane.showMessageDialog(delegado, textos.getString("msg.error.eliminar"));
        }
    }

   //Se eliminan los métodos de CSV porque ahora se usa JSON

    // Estadísticas e internacionalización
    private void actualizarEstadisticas() {
        int total = contactos.size();
        int favoritos = 0, amigos = 0, trabajo = 0, familia = 0;
        for (persona p : contactos) {
            if (p.isFavorito()) favoritos++;
            switch (p.getCategoria().toLowerCase()) {
                case "amigo": amigos++; break;
                case "trabajo": trabajo++; break;
                case "familia": familia++; break;
            }
        }
        if (textos != null) {
            delegado.lbl_total.setText(textos.getString("stat.total") + total);
            delegado.lbl_favoritos.setText(textos.getString("stat.fav") + favoritos);
            delegado.lbl_amigos.setText(textos.getString("stat.amigo") + amigos);
            delegado.lbl_trabajo.setText(textos.getString("stat.trabajo") + trabajo);
            delegado.lbl_familia.setText(textos.getString("stat.familia") + familia);
        }
    }

    private void mostrarNotificacionTemporal(String mensaje, boolean exito) {
        delegado.progressBar.setString(mensaje);
        delegado.progressBar.setForeground(exito ? Color.GREEN : Color.RED);
        new Thread(() -> {
            try { Thread.sleep(2000); } catch (InterruptedException e) {}
            SwingUtilities.invokeLater(() -> {
                delegado.progressBar.setString("Listo");
                delegado.progressBar.setForeground(UIManager.getColor("ProgressBar.foreground"));
            });
        }).start();
    }

    // ================================
    // EVENTO PRINCIPAL DE ACTIONLISTENER
    // ================================
    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == delegado.btn_add) agregarContacto();
        else if (src == delegado.btn_modificar) modificarContacto();
        else if (src == delegado.btn_eliminar) eliminarSeleccionado();
        else if (src == delegado.btn_importarJson) importarJSON();
        else if (src == delegado.btn_exportarJson) exportarJSON();
    }
    private void toggleTheme() {
        boolean isDark = delegado.btnToggleTheme.isSelected();
        try {
            if (isDark) {
                FlatDarkLaf.setup();
                delegado.btnToggleTheme.setText("🌙");
                delegado.btnToggleTheme.setToolTipText("Modo oscuro");
            } else {
                FlatLightLaf.setup();
                delegado.btnToggleTheme.setText("🌞");
                delegado.btnToggleTheme.setToolTipText("Modo claro");
            }
            // Forzar actualización de todos los componentes de la ventana
            SwingUtilities.updateComponentTreeUI(delegado);
            delegado.repaint();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}