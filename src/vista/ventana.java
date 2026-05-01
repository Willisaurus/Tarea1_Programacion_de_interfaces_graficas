package vista;

import controlador.logica_ventana;

import java.awt.*;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

/**
 * GESTIÓN DE CONTACTOS – Vista principal (MVC)
 *
 * Mejoras respecto a la versión anterior:
 *  – GridBagLayout en el formulario (reemplaza GridLayout fijo de 3×4).
 *  – Soporte de tema Claro / Oscuro mediante ThemeManager (paleta Catppuccin Mocha).
 *  – Internacionalización con ResourceBundle: Español, Inglés y Japonés.
 *  – Selector de idioma y botón de tema en la barra superior.
 *  – Botones de acción con colores semánticos y estilo compacto.
 *  – Tabla con cabecera destacada, filas alternas y renderer de favoritos (★/☆).
 *  – Barra de estadísticas renovada en la segunda pestaña.
 */
public class ventana extends JFrame {

    // ── Valores canónicos de categoría (persistidos en CSV, siempre en español) ─
    public static final String[] CATEGORY_KEYS = { "Amigo", "Trabajo", "Familia", "Otro" };

    // ── Locales disponibles ────────────────────────────────────────────────────
    private static final String[] LOCALE_CODES  = { "es", "en", "ja" };
    private static final String[] LOCALE_LABELS = { "Español", "English", "日本語" };

    // ═══════════════════════════════════════════════════════════════════════════
    //  COMPONENTES PÚBLICOS (accedidos por el controlador)
    // ═══════════════════════════════════════════════════════════════════════════
    public JPanel            contentPane;

    // Formulario
    public JTextField        txt_nombres;
    public JTextField        txt_telefono;
    public JTextField        txt_email;
    public JTextField        txt_buscar;
    public JCheckBox         chb_favorito;
    public JComboBox<String> cmb_categoria;

    // Botones de acción
    public JButton           btn_add;
    public JButton           btn_modificar;
    public JButton           btn_eliminar;
    public JButton           btn_exportar;
    public JButton           btn_importar;

    // Controles de la cabecera
    public JButton           btn_tema;
    public JComboBox<String> cmb_idioma;

    // Tabla
    public JTable            tbl_contactos;
    public DefaultTableModel tableModel;
    public JScrollPane       scrTabla;

    // Pestañas / progreso
    public JTabbedPane       tabbedPane;
    public JProgressBar      progressBar;

    // Estadísticas
    public JLabel lbl_total;
    public JLabel lbl_favoritos;
    public JLabel lbl_amigos;
    public JLabel lbl_trabajo;
    public JLabel lbl_familia;

    // ═══════════════════════════════════════════════════════════════════════════
    //  COMPONENTES PRIVADOS (sólo para aplicar tema)
    // ═══════════════════════════════════════════════════════════════════════════
    private JPanel headerPanel;
    private JPanel panelContactos;
    private JPanel panelStats;
    private JPanel panelFormulario;
    private JPanel panelBotones;
    private JPanel panelInferior;

    private JLabel lblAppTitle;
    private JLabel lblIdiomaLbl;
    private JLabel lblNombreF;
    private JLabel lblTelefonoF;
    private JLabel lblEmailF;
    private JLabel lblCategoriaF;
    private JLabel lblFavoritoF;
    private JLabel lblBuscarF;

    // ─── i18n ──────────────────────────────────────────────────────────────────
    private ResourceBundle bundle;
    public  Locale         currentLocale;

    // ═══════════════════════════════════════════════════════════════════════════
    //  PUNTO DE ENTRADA
    // ═══════════════════════════════════════════════════════════════════════════
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignored) { }

            try {
                ventana frame = new ventana();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════════════════════
    public ventana() {
        currentLocale = new Locale("es");
        bundle = ResourceBundle.getBundle("i18n.messages", currentLocale, new UTF8Control());

        initUI();
        applyTheme();
        new logica_ventana(this);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  CONSTRUCCIÓN DE LA INTERFAZ
    // ═══════════════════════════════════════════════════════════════════════════
    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(980, 660));
        setBounds(100, 100, 1120, 760);
        setTitle(bundle.getString("app.title"));

        contentPane = new JPanel(new BorderLayout());
        setContentPane(contentPane);

        buildHeader();
        buildTabs();
    }

    // ── Cabecera ───────────────────────────────────────────────────────────────
    private void buildHeader() {
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(new EmptyBorder(10, 18, 10, 18));

        lblAppTitle = new JLabel("\u2756 " + bundle.getString("app.title"));
        lblAppTitle.setFont(new Font("Segoe UI", Font.BOLD, 21));

        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightControls.setOpaque(false);

        lblIdiomaLbl = new JLabel(bundle.getString("lbl.language"));
        lblIdiomaLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        cmb_idioma = new JComboBox<>(LOCALE_LABELS);
        cmb_idioma.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cmb_idioma.setPreferredSize(new Dimension(110, 28));
        cmb_idioma.setFocusable(false);

        btn_tema = new JButton();
        btn_tema.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn_tema.setFocusPainted(false);
        btn_tema.setBorderPainted(false);
        btn_tema.setOpaque(true);
        btn_tema.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn_tema.setPreferredSize(new Dimension(150, 28));
        updateThemeButtonText();

        rightControls.add(lblIdiomaLbl);
        rightControls.add(cmb_idioma);
        rightControls.add(Box.createHorizontalStrut(4));
        rightControls.add(btn_tema);

        headerPanel.add(lblAppTitle,   BorderLayout.WEST);
        headerPanel.add(rightControls, BorderLayout.EAST);
        contentPane.add(headerPanel, BorderLayout.NORTH);
    }

    // ── Pestañas ───────────────────────────────────────────────────────────────
    private void buildTabs() {
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));
        contentPane.add(tabbedPane, BorderLayout.CENTER);
        buildContactsTab();
        buildStatsTab();
    }

    // ── Pestaña Contactos ──────────────────────────────────────────────────────
    private void buildContactsTab() {
        panelContactos = new JPanel(new BorderLayout(0, 8));
        panelContactos.setBorder(new EmptyBorder(10, 10, 10, 10));
        tabbedPane.addTab(bundle.getString("tab.contacts"), panelContactos);

        buildForm();
        buildTable();
        buildButtonBar();
    }

    private void buildForm() {
        panelFormulario = new JPanel(new GridBagLayout());
        panelFormulario.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1, true),
                new EmptyBorder(10, 14, 10, 14)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 8);
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        // Fila 0: Nombre | Teléfono
        lblNombreF   = newFormLabel(bundle.getString("label.name"));
        txt_nombres  = newTextField();
        lblTelefonoF = newFormLabel(bundle.getString("label.phone"));
        txt_telefono = newTextField();
        addRow(panelFormulario, gbc, 0, lblNombreF, txt_nombres, lblTelefonoF, txt_telefono);

        // Fila 1: Email | Categoría
        lblEmailF     = newFormLabel(bundle.getString("label.email"));
        txt_email     = newTextField();
        lblCategoriaF = newFormLabel(bundle.getString("label.category"));
        cmb_categoria = new JComboBox<>(getCategoryDisplayNames());
        cmb_categoria.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        addRow(panelFormulario, gbc, 1, lblEmailF, txt_email, lblCategoriaF, cmb_categoria);

        // Fila 2: Favorito | Buscar
        lblFavoritoF = newFormLabel(bundle.getString("label.favorite"));
        chb_favorito = new JCheckBox(bundle.getString("chk.favorite"));
        chb_favorito.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chb_favorito.setOpaque(false);
        lblBuscarF = newFormLabel(bundle.getString("label.search"));
        txt_buscar = newTextField();
        addRow(panelFormulario, gbc, 2, lblFavoritoF, chb_favorito, lblBuscarF, txt_buscar);

        panelContactos.add(panelFormulario, BorderLayout.NORTH);
    }

    /** Añade lbl1 | comp1 | lbl2 | comp2 en una fila del GridBagLayout. */
    private void addRow(JPanel p, GridBagConstraints gbc, int row,
                        JComponent lbl1, JComponent c1,
                        JComponent lbl2, JComponent c2) {
        gbc.gridy = row;
        gbc.gridx = 0; gbc.weightx = 0;    p.add(lbl1, gbc);
        gbc.gridx = 1; gbc.weightx = 0.4;  p.add(c1,   gbc);
        gbc.gridx = 2; gbc.weightx = 0;    p.add(lbl2, gbc);
        gbc.gridx = 3; gbc.weightx = 0.4;  p.add(c2,   gbc);
    }

    private void buildTable() {
        tableModel = new DefaultTableModel(
                new String[]{
                        bundle.getString("col.name"),
                        bundle.getString("col.phone"),
                        bundle.getString("col.email"),
                        bundle.getString("col.category"),
                        bundle.getString("col.favorite")
                }, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                return c == 4 ? Boolean.class : String.class;
            }
        };

        tbl_contactos = new JTable(tableModel);
        tbl_contactos.setRowHeight(28);
        tbl_contactos.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tbl_contactos.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tbl_contactos.getTableHeader().setReorderingAllowed(false);
        tbl_contactos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tbl_contactos.setIntercellSpacing(new Dimension(10, 0));
        tbl_contactos.setShowHorizontalLines(true);
        tbl_contactos.setShowVerticalLines(false);

        applyTableRenderer();

        scrTabla = new JScrollPane(tbl_contactos);
        scrTabla.getVerticalScrollBar().setUnitIncrement(16);
        panelContactos.add(scrTabla, BorderLayout.CENTER);
    }

    /** Crea/recrea los renderers de la tabla con los colores del tema activo. */
    public void applyTableRenderer() {
        DefaultTableCellRenderer altRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                if (!sel) {
                    c.setBackground(row % 2 == 0
                            ? ThemeManager.getTableRowBg()
                            : ThemeManager.getTableAltRowBg());
                    c.setForeground(ThemeManager.getText());
                } else {
                    c.setBackground(ThemeManager.getTableSelectionBg());
                    c.setForeground(Color.WHITE);
                }
                return c;
            }
        };
        altRenderer.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tbl_contactos.setDefaultRenderer(Object.class,  altRenderer);
        tbl_contactos.setDefaultRenderer(String.class,  altRenderer);

        // Columna de favorito: estrella rellena / vacía
        tbl_contactos.setDefaultRenderer(Boolean.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                JLabel lbl = new JLabel(Boolean.TRUE.equals(val) ? "\u2605" : "\u2606");
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                lbl.setOpaque(true);
                if (!sel) {
                    lbl.setBackground(row % 2 == 0
                            ? ThemeManager.getTableRowBg()
                            : ThemeManager.getTableAltRowBg());
                    lbl.setForeground(ThemeManager.getAccent());
                } else {
                    lbl.setBackground(ThemeManager.getTableSelectionBg());
                    lbl.setForeground(Color.WHITE);
                }
                return lbl;
            }
        });
    }

    private void buildButtonBar() {
        panelInferior = new JPanel(new BorderLayout());
        panelBotones  = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));

        btn_add       = newActionBtn("btn.add",    "\u271A", ThemeManager.getBtnAdd());
        btn_modificar = newActionBtn("btn.modify", "\u270E", ThemeManager.getBtnModify());
        btn_eliminar  = newActionBtn("btn.delete", "\u2715", ThemeManager.getBtnDelete());
        btn_exportar  = newActionBtn("btn.export", "\u2191", ThemeManager.getBtnExport());
        btn_importar  = newActionBtn("btn.import", "\u2193", ThemeManager.getBtnImport());

        panelBotones.add(btn_add);
        panelBotones.add(btn_modificar);
        panelBotones.add(btn_eliminar);
        panelBotones.add(btn_exportar);
        panelBotones.add(btn_importar);

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setString(bundle.getString("status.ready"));
        progressBar.setPreferredSize(new Dimension(200, 20));
        progressBar.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        panelInferior.add(panelBotones, BorderLayout.CENTER);
        panelInferior.add(progressBar,  BorderLayout.SOUTH);
        panelContactos.add(panelInferior, BorderLayout.SOUTH);
    }

    // ── Pestaña Estadísticas ───────────────────────────────────────────────────
    private void buildStatsTab() {
        panelStats = new JPanel(new GridLayout(5, 1, 0, 8));
        panelStats.setBorder(new EmptyBorder(18, 22, 18, 22));
        tabbedPane.addTab(bundle.getString("tab.stats"), panelStats);

        lbl_total     = newStatLabel("\u25B6 ");
        lbl_favoritos = newStatLabel("\u2605 ");
        lbl_amigos    = newStatLabel("\u25CF ");
        lbl_trabajo   = newStatLabel("\u25CF ");
        lbl_familia   = newStatLabel("\u25CF ");

        panelStats.add(lbl_total);
        panelStats.add(lbl_favoritos);
        panelStats.add(lbl_amigos);
        panelStats.add(lbl_trabajo);
        panelStats.add(lbl_familia);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  APLICACIÓN DE TEMA
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Aplica la paleta de colores del tema activo a todos los componentes.
     * Se llama al iniciar y cada vez que el usuario pulsa el botón de tema.
     */
    public void applyTheme() {
        Color bg      = ThemeManager.getBg();
        Color panelBg = ThemeManager.getPanelBg();
        Color fieldBg = ThemeManager.getFieldBg();
        Color text    = ThemeManager.getText();
        Color accent  = ThemeManager.getAccent();
        Color border  = ThemeManager.getBorderColor();

        contentPane.setBackground(bg);

        // Cabecera
        headerPanel.setBackground(panelBg);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, border),
                new EmptyBorder(10, 18, 10, 18)));
        lblAppTitle.setForeground(accent);
        lblIdiomaLbl.setForeground(text);
        styleCombo(cmb_idioma, fieldBg, text);
        styleBtn(btn_tema, ThemeManager.getBtnTheme(), Color.WHITE);
        updateThemeButtonText();

        // Pestañas
        tabbedPane.setBackground(bg);
        tabbedPane.setForeground(text);

        // Panel contactos
        panelContactos.setBackground(bg);

        // Formulario
        panelFormulario.setBackground(panelBg);
        panelFormulario.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(border, 1, true),
                new EmptyBorder(10, 14, 10, 14)));

        for (JLabel lbl : new JLabel[]{ lblNombreF, lblTelefonoF, lblEmailF,
                                        lblCategoriaF, lblFavoritoF, lblBuscarF }) {
            lbl.setForeground(text);
        }

        for (JTextField tf : new JTextField[]{ txt_nombres, txt_telefono, txt_email, txt_buscar }) {
            tf.setBackground(fieldBg);
            tf.setForeground(text);
            tf.setCaretColor(text);
            tf.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(border, 1, true),
                    new EmptyBorder(3, 8, 3, 8)));
        }

        chb_favorito.setBackground(panelBg);
        chb_favorito.setForeground(text);
        styleCombo(cmb_categoria, fieldBg, text);

        // Tabla
        tbl_contactos.setBackground(ThemeManager.getTableRowBg());
        tbl_contactos.setForeground(text);
        tbl_contactos.setGridColor(ThemeManager.getTableGridColor());
        tbl_contactos.setSelectionBackground(ThemeManager.getTableSelectionBg());
        tbl_contactos.setSelectionForeground(Color.WHITE);
        tbl_contactos.getTableHeader().setBackground(ThemeManager.getTableHeaderBg());
        tbl_contactos.getTableHeader().setForeground(ThemeManager.getTableHeaderFg());
        scrTabla.getViewport().setBackground(ThemeManager.getTableRowBg());
        scrTabla.setBackground(ThemeManager.getTableRowBg());
        scrTabla.setBorder(BorderFactory.createLineBorder(border, 1));
        applyTableRenderer();

        // Botones de acción
        panelBotones.setBackground(bg);
        panelInferior.setBackground(bg);
        styleBtn(btn_add,       ThemeManager.getBtnAdd(),    Color.WHITE);
        styleBtn(btn_modificar, ThemeManager.getBtnModify(), Color.WHITE);
        styleBtn(btn_eliminar,  ThemeManager.getBtnDelete(), Color.WHITE);
        styleBtn(btn_exportar,  ThemeManager.getBtnExport(), Color.WHITE);
        styleBtn(btn_importar,  ThemeManager.getBtnImport(), Color.WHITE);

        // Barra de progreso
        progressBar.setBackground(panelBg);
        progressBar.setForeground(accent);

        // Estadísticas
        panelStats.setBackground(bg);
        for (JLabel lbl : new JLabel[]{ lbl_total, lbl_favoritos,
                                        lbl_amigos, lbl_trabajo, lbl_familia }) {
            lbl.setForeground(text);
        }

        revalidate();
        repaint();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  INTERNACIONALIZACIÓN
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Cambia el idioma de toda la interfaz sin recrear los componentes.
     *
     * @param locale nuevo Locale ({@code es}, {@code en}, {@code ja})
     */
    public void updateTexts(Locale locale) {
        currentLocale = locale;
        bundle = ResourceBundle.getBundle("i18n.messages", locale, new UTF8Control());

        setTitle(bundle.getString("app.title"));
        lblAppTitle.setText("\u2756 " + bundle.getString("app.title"));
        lblIdiomaLbl.setText(bundle.getString("lbl.language"));

        tabbedPane.setTitleAt(0, bundle.getString("tab.contacts"));
        tabbedPane.setTitleAt(1, bundle.getString("tab.stats"));

        lblNombreF.setText(bundle.getString("label.name"));
        lblTelefonoF.setText(bundle.getString("label.phone"));
        lblEmailF.setText(bundle.getString("label.email"));
        lblCategoriaF.setText(bundle.getString("label.category"));
        lblFavoritoF.setText(bundle.getString("label.favorite"));
        lblBuscarF.setText(bundle.getString("label.search"));
        chb_favorito.setText(bundle.getString("chk.favorite"));

        // Actualizar combo de categoría conservando la selección
        int savedIdx = cmb_categoria.getSelectedIndex();
        cmb_categoria.removeAllItems();
        for (String cat : getCategoryDisplayNames()) cmb_categoria.addItem(cat);
        if (savedIdx >= 0 && savedIdx < cmb_categoria.getItemCount()) {
            cmb_categoria.setSelectedIndex(savedIdx);
        }

        // Cabeceras de la tabla
        tableModel.setColumnIdentifiers(new String[]{
                bundle.getString("col.name"),
                bundle.getString("col.phone"),
                bundle.getString("col.email"),
                bundle.getString("col.category"),
                bundle.getString("col.favorite")
        });

        btn_add.setText("\u271A " + bundle.getString("btn.add"));
        btn_modificar.setText("\u270E " + bundle.getString("btn.modify"));
        btn_eliminar.setText("\u2715 " + bundle.getString("btn.delete"));
        btn_exportar.setText("\u2191 " + bundle.getString("btn.export"));
        btn_importar.setText("\u2193 " + bundle.getString("btn.import"));

        if (!progressBar.isIndeterminate()) {
            progressBar.setString(bundle.getString("status.ready"));
        }

        updateThemeButtonText();
    }

    // ─── Acceso público al bundle ──────────────────────────────────────────────
    public ResourceBundle getBundle() { return bundle; }

    /** Código ISO del idioma actualmente elegido en el ComboBox. */
    public String getSelectedLocaleCode() {
        int idx = cmb_idioma.getSelectedIndex();
        return (idx >= 0 && idx < LOCALE_CODES.length) ? LOCALE_CODES[idx] : "es";
    }

    /** Nombres de categoría en el idioma actual para mostrar en la UI. */
    public String[] getCategoryDisplayNames() {
        return new String[]{
                bundle.getString("cat.friend"),
                bundle.getString("cat.work"),
                bundle.getString("cat.family"),
                bundle.getString("cat.other")
        };
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  FÁBRICA DE COMPONENTES Y HELPERS DE ESTILO
    // ═══════════════════════════════════════════════════════════════════════════

    private JLabel newFormLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return lbl;
    }

    private JTextField newTextField() {
        JTextField tf = new JTextField();
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        return tf;
    }

    private JButton newActionBtn(String key, String icon, Color bg) {
        JButton btn = new JButton(icon + " " + bundle.getString(key));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setBorder(new EmptyBorder(5, 14, 5, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JLabel newStatLabel(String prefix) {
        JLabel lbl = new JLabel(prefix + "-");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        return lbl;
    }

    private void styleCombo(JComboBox<?> combo, Color bg, Color fg) {
        combo.setBackground(bg);
        combo.setForeground(fg);
    }

    private void styleBtn(JButton btn, Color bg, Color fg) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
    }

    private void updateThemeButtonText() {
        if (ThemeManager.isDark()) {
            btn_tema.setText("\u2600 " + bundle.getString("btn.theme.light"));
        } else {
            btn_tema.setText("\u263D " + bundle.getString("btn.theme.dark"));
        }
    }
}
