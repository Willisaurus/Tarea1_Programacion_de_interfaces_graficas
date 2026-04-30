package controlador;

import modelo.persona;
import modelo.personaDAO;
import vista.ThemeManager;
import vista.ventana;

import javax.swing.*;
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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

/**
 * Controlador principal – conecta la Vista (ventana) con el Modelo (persona / personaDAO).
 *
 * Cambios respecto a la versión anterior:
 *  – Toda cadena de texto visible obtiene su valor del ResourceBundle activo
 *    para soportar los tres idiomas (ES / EN / JA).
 *  – Se añaden listeners para el selector de idioma (cmb_idioma) y el botón
 *    de tema (btn_tema).
 *  – translateCategory() convierte el valor canónico (español, almacenado en
 *    CSV) al nombre localizado que se muestra en la tabla.
 *  – construirDesdeFormulario() usa el índice del ComboBox para obtener siempre
 *    el valor canónico, garantizando compatibilidad con el CSV.
 */
public class logica_ventana implements ActionListener {

    private final ventana    delegado;
    private final personaDAO dao;
    private List<persona>    contactos;
    private TableRowSorter<?>  sorter;

    // Ítems del menú contextual (se actualizan al cambiar idioma)
    private JMenuItem menuItemEditar;
    private JMenuItem menuItemEliminar;
    private JPopupMenu popupMenu;

    public logica_ventana(ventana delegado) {
        this.delegado  = delegado;
        this.dao       = new personaDAO();
        this.contactos = new ArrayList<>();

        configurarEventos();
        configurarTablaYFiltro();
        configurarAtajosTeclado();
        configurarMenuContextual();
        procesarArchivoEnSegundoPlano(null, false);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  CONFIGURACIÓN INICIAL
    // ═══════════════════════════════════════════════════════════════════════════

    private void configurarEventos() {
        // Botones de acción
        delegado.btn_add.addActionListener(this);
        delegado.btn_modificar.addActionListener(this);
        delegado.btn_eliminar.addActionListener(this);
        delegado.btn_exportar.addActionListener(this);
        delegado.btn_importar.addActionListener(this);

        // Selección en la tabla → carga el formulario
        delegado.tbl_contactos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) cargarFilaEnFormulario();
        });

        // Búsqueda en tiempo real
        delegado.txt_buscar.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { aplicarFiltro(); }
            public void removeUpdate(DocumentEvent e) { aplicarFiltro(); }
            public void changedUpdate(DocumentEvent e) { aplicarFiltro(); }
        });

        // ── Selector de idioma ────────────────────────────────────────────────
        delegado.cmb_idioma.addActionListener(e -> {
            String code   = delegado.getSelectedLocaleCode();
            Locale locale = new Locale(code);
            delegado.updateTexts(locale);
            delegado.applyTheme();      // reaplica colores (botón tema cambia texto)
            refrescarTabla();           // actualiza categorías traducidas en tabla
            actualizarEstadisticas();   // actualiza etiquetas de stats
            actualizarMenuContextual(); // actualiza textos del menú contextual
        });

        // ── Botón de tema ─────────────────────────────────────────────────────
        delegado.btn_tema.addActionListener(e -> {
            ThemeManager.toggleTheme();
            delegado.applyTheme();
        });
    }

    private void configurarTablaYFiltro() {
        sorter = new TableRowSorter<>(delegado.tableModel);
        delegado.tbl_contactos.setRowSorter(sorter);
    }

    private void configurarAtajosTeclado() {
        InputMap im = delegado.contentPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = delegado.contentPane.getActionMap();

        // Ctrl+N → nuevo / limpiar campos
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK), "nuevo");
        am.put("nuevo", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { limpiarCampos(); }
        });

        // Ctrl+E → exportar CSV
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK), "exportar");
        am.put("exportar", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { exportarCSV(); }
        });

        // Supr → eliminar contacto seleccionado
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "eliminar");
        am.put("eliminar", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { eliminarSeleccionado(); }
        });

        // Ctrl+F → enfocar buscador
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK), "buscar");
        am.put("buscar", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { delegado.txt_buscar.requestFocus(); }
        });
    }

    private void configurarMenuContextual() {
        popupMenu = new JPopupMenu();
        menuItemEditar   = new JMenuItem(bundle().getString("menu.edit"));
        menuItemEliminar = new JMenuItem(bundle().getString("menu.delete"));

        menuItemEditar.addActionListener(e   -> cargarFilaEnFormulario());
        menuItemEliminar.addActionListener(e -> eliminarSeleccionado());

        popupMenu.add(menuItemEditar);
        popupMenu.add(menuItemEliminar);

        delegado.tbl_contactos.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e)  { mostrarPopup(e); }
            @Override public void mouseReleased(MouseEvent e) { mostrarPopup(e); }

            private void mostrarPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int row = delegado.tbl_contactos.rowAtPoint(e.getPoint());
                    if (row >= 0) delegado.tbl_contactos.setRowSelectionInterval(row, row);
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }

    /** Actualiza los textos del menú contextual cuando cambia el idioma. */
    private void actualizarMenuContextual() {
        menuItemEditar.setText(bundle().getString("menu.edit"));
        menuItemEliminar.setText(bundle().getString("menu.delete"));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  CARGA / IMPORTACIÓN EN SEGUNDO PLANO
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Usa un SwingWorker para no bloquear el EDT durante la lectura del CSV.
     * El mismo método sirve para la carga inicial y para la importación manual.
     *
     * @param archivoImportar archivo CSV externo; {@code null} para carga inicial
     * @param esImportacion   {@code true} si el usuario seleccionó un archivo
     */
    private void procesarArchivoEnSegundoPlano(File archivoImportar, boolean esImportacion) {
        delegado.progressBar.setIndeterminate(true);
        delegado.progressBar.setString(esImportacion
                ? bundle().getString("status.importing")
                : bundle().getString("status.loading"));

        SwingWorker<List<persona>, Void> worker = new SwingWorker<List<persona>, Void>() {
            @Override
            protected List<persona> doInBackground() throws Exception {
                // Pausa mínima para que se vea la animación de la barra
                Thread.sleep(600);
                return esImportacion && archivoImportar != null
                        ? dao.leerCualquierArchivo(archivoImportar)
                        : dao.leerArchivo();
            }

            @Override
            protected void done() {
                delegado.progressBar.setIndeterminate(false);
                delegado.progressBar.setValue(100);

                try {
                    List<persona> datosNuevos = get();

                    if (esImportacion) {
                        contactos.addAll(datosNuevos);
                        dao.guardarTodos(contactos);
                        JOptionPane.showMessageDialog(delegado,
                                MessageFormat.format(bundle().getString("msg.import.success"),
                                        datosNuevos.size()));
                    } else {
                        contactos = datosNuevos;
                    }

                    refrescarTabla();
                    actualizarEstadisticas();
                    delegado.progressBar.setString(bundle().getString("status.ready"));

                } catch (Exception ex) {
                    delegado.progressBar.setString(bundle().getString("status.error"));
                    JOptionPane.showMessageDialog(delegado,
                            bundle().getString("msg.import.error"));
                }
            }
        };
        worker.execute();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  OPERACIONES CRUD
    // ═══════════════════════════════════════════════════════════════════════════

    private void agregarContacto() {
        if (!validarCampos()) return;
        contactos.add(construirDesdeFormulario());

        if (dao.guardarTodos(contactos)) {
            refrescarTabla();
            actualizarEstadisticas();
            limpiarCampos();
            JOptionPane.showMessageDialog(delegado, bundle().getString("msg.added"));
        } else {
            JOptionPane.showMessageDialog(delegado, bundle().getString("msg.save.error"));
        }
    }

    private void modificarContacto() {
        int rowView = delegado.tbl_contactos.getSelectedRow();
        if (rowView < 0) {
            JOptionPane.showMessageDialog(delegado, bundle().getString("msg.select.modify"));
            return;
        }
        if (!validarCampos()) return;

        int rowModel = delegado.tbl_contactos.convertRowIndexToModel(rowView);
        contactos.set(rowModel, construirDesdeFormulario());

        if (dao.guardarTodos(contactos)) {
            refrescarTabla();
            actualizarEstadisticas();
            JOptionPane.showMessageDialog(delegado, bundle().getString("msg.modified"));
        } else {
            JOptionPane.showMessageDialog(delegado, bundle().getString("msg.modify.error"));
        }
    }

    private void eliminarSeleccionado() {
        int rowView = delegado.tbl_contactos.getSelectedRow();
        if (rowView < 0) {
            JOptionPane.showMessageDialog(delegado, bundle().getString("msg.select.delete"));
            return;
        }

        int resp = JOptionPane.showConfirmDialog(delegado,
                bundle().getString("msg.confirm.delete"),
                bundle().getString("msg.confirm.title"),
                JOptionPane.YES_NO_OPTION);
        if (resp != JOptionPane.YES_OPTION) return;

        int rowModel = delegado.tbl_contactos.convertRowIndexToModel(rowView);
        contactos.remove(rowModel);

        if (dao.guardarTodos(contactos)) {
            refrescarTabla();
            actualizarEstadisticas();
            limpiarCampos();
            JOptionPane.showMessageDialog(delegado, bundle().getString("msg.deleted"));
        } else {
            JOptionPane.showMessageDialog(delegado, bundle().getString("msg.delete.error"));
        }
    }

    private void exportarCSV() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(bundle().getString("dialog.export.title"));
        chooser.setSelectedFile(new File("contactos_exportados.csv"));

        if (chooser.showSaveDialog(delegado) == JFileChooser.APPROVE_OPTION) {
            File destino = chooser.getSelectedFile();
            if (dao.exportarCSV(contactos, destino)) {
                JOptionPane.showMessageDialog(delegado,
                        MessageFormat.format(bundle().getString("msg.export.success"),
                                destino.getAbsolutePath()));
            } else {
                JOptionPane.showMessageDialog(delegado, bundle().getString("msg.export.error"));
            }
        }
    }

    private void importarCSV() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(bundle().getString("dialog.import.title"));

        if (chooser.showOpenDialog(delegado) == JFileChooser.APPROVE_OPTION) {
            procesarArchivoEnSegundoPlano(chooser.getSelectedFile(), true);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  HELPERS DE DATOS Y UI
    // ═══════════════════════════════════════════════════════════════════════════

    /** Rellena la tabla con los contactos actuales y traduce las categorías. */
    private void refrescarTabla() {
        delegado.tableModel.setRowCount(0);
        for (persona p : contactos) {
            delegado.tableModel.addRow(new Object[]{
                    p.getNombre(),
                    p.getTelefono(),
                    p.getEmail(),
                    translateCategory(p.getCategoria()),
                    p.isFavorito()
            });
        }
    }

    /**
     * Convierte el valor canónico de categoría (guardado en español en el CSV)
     * al nombre localizado para mostrarlo en la tabla.
     *
     * @param storedKey valor leído del CSV ("Amigo", "Trabajo", "Familia", "Otro")
     * @return nombre de la categoría en el idioma activo
     */
    private String translateCategory(String storedKey) {
        String[] display = delegado.getCategoryDisplayNames();
        for (int i = 0; i < ventana.CATEGORY_KEYS.length; i++) {
            if (ventana.CATEGORY_KEYS[i].equalsIgnoreCase(storedKey)) {
                return display[i];
            }
        }
        return storedKey; // fallback: devuelve el valor tal cual
    }

    /** Aplica el filtro de búsqueda sobre todas las columnas de la tabla. */
    private void aplicarFiltro() {
        String texto = delegado.txt_buscar.getText().trim();
        if (texto.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(texto)));
        }
    }

    /** Valida que los campos obligatorios no estén vacíos. */
    private boolean validarCampos() {
        if (delegado.txt_nombres.getText().trim().isEmpty() ||
                delegado.txt_telefono.getText().trim().isEmpty() ||
                delegado.txt_email.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(delegado, bundle().getString("msg.fill.fields"));
            return false;
        }
        return true;
    }

    /**
     * Construye un objeto {@link persona} desde los campos del formulario.
     * La categoría siempre se guarda con el valor canónico en español para
     * mantener compatibilidad con el archivo CSV.
     */
    private persona construirDesdeFormulario() {
        int catIdx = delegado.cmb_categoria.getSelectedIndex();
        // CATEGORY_KEYS garantiza el valor canónico independientemente del idioma
        String categoria = (catIdx >= 0 && catIdx < ventana.CATEGORY_KEYS.length)
                ? ventana.CATEGORY_KEYS[catIdx]
                : delegado.cmb_categoria.getSelectedItem().toString();

        return new persona(
                delegado.txt_nombres.getText().trim(),
                delegado.txt_telefono.getText().trim(),
                delegado.txt_email.getText().trim(),
                categoria,
                delegado.chb_favorito.isSelected()
        );
    }

    /** Limpia todos los campos del formulario. */
    private void limpiarCampos() {
        delegado.txt_nombres.setText("");
        delegado.txt_telefono.setText("");
        delegado.txt_email.setText("");
        delegado.cmb_categoria.setSelectedIndex(0);
        delegado.chb_favorito.setSelected(false);
        delegado.tbl_contactos.clearSelection();
        delegado.txt_nombres.requestFocus();
    }

    /**
     * Carga los datos de la fila seleccionada en el formulario.
     * Usa la traducción inversa para seleccionar el índice correcto del combo.
     */
    private void cargarFilaEnFormulario() {
        int rowView = delegado.tbl_contactos.getSelectedRow();
        if (rowView < 0) return;

        int rowModel = delegado.tbl_contactos.convertRowIndexToModel(rowView);

        delegado.txt_nombres.setText(
                String.valueOf(delegado.tableModel.getValueAt(rowModel, 0)));
        delegado.txt_telefono.setText(
                String.valueOf(delegado.tableModel.getValueAt(rowModel, 1)));
        delegado.txt_email.setText(
                String.valueOf(delegado.tableModel.getValueAt(rowModel, 2)));

        // La columna 3 contiene el nombre localizado → buscar el índice correcto
        String displayCat = String.valueOf(delegado.tableModel.getValueAt(rowModel, 3));
        String[] displayNames = delegado.getCategoryDisplayNames();
        int catIdx = -1;
        for (int i = 0; i < displayNames.length; i++) {
            if (displayNames[i].equals(displayCat)) { catIdx = i; break; }
        }
        if (catIdx >= 0) delegado.cmb_categoria.setSelectedIndex(catIdx);

        delegado.chb_favorito.setSelected(
                Boolean.parseBoolean(
                        String.valueOf(delegado.tableModel.getValueAt(rowModel, 4))));
    }

    /** Actualiza las etiquetas de la pestaña de estadísticas. */
    private void actualizarEstadisticas() {
        int total = contactos.size();
        int favoritos = 0, amigos = 0, trabajo = 0, familia = 0;

        for (persona p : contactos) {
            if (p.isFavorito()) favoritos++;
            // Comparación con CATEGORY_KEYS (valor canónico en español)
            String cat = p.getCategoria();
            if ("Amigo".equalsIgnoreCase(cat))   amigos++;
            if ("Trabajo".equalsIgnoreCase(cat))  trabajo++;
            if ("Familia".equalsIgnoreCase(cat))  familia++;
        }

        delegado.lbl_total.setText("\u25B6 " +
                MessageFormat.format(bundle().getString("stats.total"), total));
        delegado.lbl_favoritos.setText("\u2605 " +
                MessageFormat.format(bundle().getString("stats.favorites"), favoritos));
        delegado.lbl_amigos.setText("\u25CF " +
                MessageFormat.format(bundle().getString("stats.friends"), amigos));
        delegado.lbl_trabajo.setText("\u25CF " +
                MessageFormat.format(bundle().getString("stats.work"), trabajo));
        delegado.lbl_familia.setText("\u25CF " +
                MessageFormat.format(bundle().getString("stats.family"), familia));
    }

    // ─── Acceso rápido al bundle activo ───────────────────────────────────────
    private ResourceBundle bundle() { return delegado.getBundle(); }

    // ═══════════════════════════════════════════════════════════════════════════
    //  ActionListener
    // ═══════════════════════════════════════════════════════════════════════════
    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if      (src == delegado.btn_add)       agregarContacto();
        else if (src == delegado.btn_modificar) modificarContacto();
        else if (src == delegado.btn_eliminar)  eliminarSeleccionado();
        else if (src == delegado.btn_exportar)  exportarCSV();
        else if (src == delegado.btn_importar)  importarCSV();
    }
}
