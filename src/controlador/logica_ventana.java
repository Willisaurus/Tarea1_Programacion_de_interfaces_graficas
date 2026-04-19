package controlador;

import modelo.persona;
import modelo.personaDAO;
import vista.ventana;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.RowFilter;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableRowSorter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class logica_ventana implements ActionListener {

    private final ventana delegado;
    private final personaDAO dao;
    private List<persona> contactos;
    private TableRowSorter sorter;

    public logica_ventana(ventana delegado) {
        this.delegado = delegado;
        this.dao = new personaDAO();
        this.contactos = new ArrayList<>();

        configurarEventos();
        configurarTablaYFiltro();
        configurarAtajosTeclado();
        configurarMenuContextual();
        cargarContactosConProgreso();
    }

    private void configurarEventos() {
        delegado.btn_add.addActionListener(this);
        delegado.btn_modificar.addActionListener(this);
        delegado.btn_eliminar.addActionListener(this);
        delegado.btn_exportar.addActionListener(this);

        /*
         * ELIMINADO DEL CÓDIGO ANTERIOR:
         * this.delegado.lst_contactos.addListSelectionListener(this);
         * implementación de ListSelectionListener centrada en JList.
         *
         * MOTIVO:
         * Ahora se usa JTable (más potente para ordenar/filtrar).
         */
        delegado.tbl_contactos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) cargarFilaEnFormulario();
        });

        // NUEVO: filtro en vivo
        delegado.txt_buscar.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { aplicarFiltro(); }
            public void removeUpdate(DocumentEvent e) { aplicarFiltro(); }
            public void changedUpdate(DocumentEvent e) { aplicarFiltro(); }
        });
    }

    private void configurarTablaYFiltro() {
        // NUEVO: ordenamiento y filtro en JTable
        sorter = new TableRowSorter(delegado.tableModel);
        delegado.tbl_contactos.setRowSorter(sorter);
    }

    private void configurarAtajosTeclado() {
        // NUEVO REQ-2: atajos de teclado
        InputMap im = delegado.contentPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = delegado.contentPane.getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK), "nuevo");
        am.put("nuevo", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { limpiarCampos(); }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK), "exportar");
        am.put("exportar", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { exportarCSV(); }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "eliminar");
        am.put("eliminar", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { eliminarSeleccionado(); }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK), "buscar");
        am.put("buscar", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { delegado.txt_buscar.requestFocus(); }
        });
    }

    private void configurarMenuContextual() {
        // NUEVO REQ-2: menú clic derecho
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

    private void cargarContactosConProgreso() {
        // NUEVO REQ-3: progreso de carga
        SwingWorker<List<persona>, Integer> worker = new SwingWorker<List<persona>, Integer>() {
            @Override
            protected List<persona> doInBackground() throws Exception {
                delegado.progressBar.setString("Cargando contactos...");
                for (int i = 0; i <= 100; i += 20) {
                    Thread.sleep(60);
                    publish(i);
                }
                return dao.leerArchivo();
            }

            @Override
            protected void process(List<Integer> chunks) {
                delegado.progressBar.setValue(chunks.get(chunks.size() - 1));
            }

            @Override
            protected void done() {
                try {
                    contactos = get();
                    refrescarTabla();
                    actualizarEstadisticas();
                    delegado.progressBar.setString("Carga completa");
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(delegado, "Error al cargar contactos.");
                    delegado.progressBar.setString("Error");
                }
            }
        };
        worker.execute();
    }

    private void refrescarTabla() {
        delegado.tableModel.setRowCount(0);
        for (persona p : contactos) {
            delegado.tableModel.addRow(new Object[]{
                    p.getNombre(), p.getTelefono(), p.getEmail(), p.getCategoria(), p.isFavorito()
            });
        }
    }

    private void aplicarFiltro() {
        String texto = delegado.txt_buscar.getText().trim();
        if (texto.isEmpty()) sorter.setRowFilter(null);
        else sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(texto)));
    }

    private boolean validarCampos() {
        if (delegado.txt_nombres.getText().trim().isEmpty() ||
                delegado.txt_telefono.getText().trim().isEmpty() ||
                delegado.txt_email.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(delegado, "Completa nombre, teléfono y email.");
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

        // NUEVO CLAVE: por ordenamiento/filtro
        int rowModel = delegado.tbl_contactos.convertRowIndexToModel(rowView);

        delegado.txt_nombres.setText(String.valueOf(delegado.tableModel.getValueAt(rowModel, 0)));
        delegado.txt_telefono.setText(String.valueOf(delegado.tableModel.getValueAt(rowModel, 1)));
        delegado.txt_email.setText(String.valueOf(delegado.tableModel.getValueAt(rowModel, 2)));
        delegado.cmb_categoria.setSelectedItem(String.valueOf(delegado.tableModel.getValueAt(rowModel, 3)));
        delegado.chb_favorito.setSelected(Boolean.parseBoolean(String.valueOf(delegado.tableModel.getValueAt(rowModel, 4))));
    }

    private void agregarContacto() {
        if (!validarCampos()) return;
        contactos.add(construirDesdeFormulario());

        if (dao.guardarTodos(contactos)) {
            refrescarTabla();
            actualizarEstadisticas();
            limpiarCampos();
            JOptionPane.showMessageDialog(delegado, "Contacto agregado.");
        } else {
            JOptionPane.showMessageDialog(delegado, "No se pudo guardar.");
        }
    }

    private void modificarContacto() {
        int rowView = delegado.tbl_contactos.getSelectedRow();
        if (rowView < 0) {
            JOptionPane.showMessageDialog(delegado, "Selecciona un contacto a modificar.");
            return;
        }
        if (!validarCampos()) return;

        int rowModel = delegado.tbl_contactos.convertRowIndexToModel(rowView);
        contactos.set(rowModel, construirDesdeFormulario());

        if (dao.guardarTodos(contactos)) {
            refrescarTabla();
            actualizarEstadisticas();
            JOptionPane.showMessageDialog(delegado, "Contacto modificado.");
        } else {
            JOptionPane.showMessageDialog(delegado, "No se pudo guardar la modificación.");
        }
    }

    private void eliminarSeleccionado() {
        int rowView = delegado.tbl_contactos.getSelectedRow();
        if (rowView < 0) {
            JOptionPane.showMessageDialog(delegado, "Selecciona un contacto a eliminar.");
            return;
        }

        int resp = JOptionPane.showConfirmDialog(delegado, "¿Eliminar contacto seleccionado?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (resp != JOptionPane.YES_OPTION) return;

        int rowModel = delegado.tbl_contactos.convertRowIndexToModel(rowView);
        contactos.remove(rowModel);

        if (dao.guardarTodos(contactos)) {
            refrescarTabla();
            actualizarEstadisticas();
            limpiarCampos();
            JOptionPane.showMessageDialog(delegado, "Contacto eliminado.");
        } else {
            JOptionPane.showMessageDialog(delegado, "No se pudo eliminar.");
        }
    }

    private void exportarCSV() {
        // NUEVO REQ-3
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Exportar contactos a CSV");
        chooser.setSelectedFile(new File("contactos_exportados.csv"));

        int opcion = chooser.showSaveDialog(delegado);
        if (opcion == JFileChooser.APPROVE_OPTION) {
            File destino = chooser.getSelectedFile();
            if (dao.exportarCSV(contactos, destino)) {
                JOptionPane.showMessageDialog(delegado, "Exportación exitosa:\n" + destino.getAbsolutePath());
            } else {
                JOptionPane.showMessageDialog(delegado, "Error al exportar CSV.");
            }
        }
    }

    private void actualizarEstadisticas() {
        // NUEVO REQ-1/3: segunda pestaña con resumen
        int total = contactos.size();
        int favoritos = 0, amigos = 0, trabajo = 0, familia = 0;

        for (persona p : contactos) {
            if (p.isFavorito()) favoritos++;
            String cat = p.getCategoria().toLowerCase();
            if ("amigo".equals(cat)) amigos++;
            if ("trabajo".equals(cat)) trabajo++;
            if ("familia".equals(cat)) familia++;
        }

        delegado.lbl_total.setText("Total contactos: " + total);
        delegado.lbl_favoritos.setText("Favoritos: " + favoritos);
        delegado.lbl_amigos.setText("Categoría Amigo: " + amigos);
        delegado.lbl_trabajo.setText("Categoría Trabajo: " + trabajo);
        delegado.lbl_familia.setText("Categoría Familia: " + familia);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == delegado.btn_add) agregarContacto();
        else if (src == delegado.btn_modificar) modificarContacto();
        else if (src == delegado.btn_eliminar) eliminarSeleccionado();
        else if (src == delegado.btn_exportar) exportarCSV();
    }
}