package vista;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ResourceBundle;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import controlador.logica_ventana;
import com.formdev.flatlaf.FlatLightLaf;  //NUEVO: FlatLaf

public class ventana extends JFrame {

    public final JToggleButton btnToggleTheme;
    public JPanel contentPane;
    public JTextField txt_nombres;
    public JTextField txt_telefono;
    public JTextField txt_email;
    public JTextField txt_buscar;
    public JCheckBox chb_favorito;
    public JComboBox<String> cmb_categoria;
    public JButton btn_add;
    public JButton btn_modificar;
    public JButton btn_eliminar;
    // NUEVOS BOTONES PARA JSON
    public JButton btn_importarJson;
    public JButton btn_exportarJson;

    public JTable tbl_contactos;
    public DefaultTableModel tableModel;
    public JScrollPane scrTabla;
    public JSplitPane splitPane;
    public JProgressBar progressBar;

    public JLabel lbl_total;
    public JLabel lbl_favoritos;
    public JLabel lbl_amigos;
    public JLabel lbl_trabajo;
    public JLabel lbl_familia;
    public JComboBox<String> cmb_idiomas;
    private JLabel lblNombre;
    private JLabel lblTelefono;
    private JLabel lblEmail;
    private JLabel lblCategoria;
    private JLabel lblFav;
    private JLabel lblBuscar;

    Color bgColor = Color.decode("#53739A");
    Color panelColor = Color.decode("#6E8CB1");
    Color primaryColor = Color.decode("#3A486B");
    Color textBoxColor = Color.decode("#9EB2CB");
    Color textColor = Color.WHITE;

    public static void main(String[] args) {
        //Aplicar FlatLaf antes de crear la GUI
        try {
            FlatLightLaf.setup();   // Look & feel moderno
        } catch (Exception e) {
            e.printStackTrace();
        }
        EventQueue.invokeLater(() -> {
            try {
                ventana frame = new ventana();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public ventana() {
        setTitle("GESTIÓN DE CONTACTOS - MVC");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);
        setBounds(100, 100, 1050, 730);

        contentPane = new JPanel(new BorderLayout());
        contentPane.setBackground(bgColor);
        contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        setContentPane(contentPane);

        // Barra superior (idiomas)
        JPanel panelTop = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelTop.setBackground(bgColor);
        cmb_idiomas = new JComboBox<>(new String[]{"Español", "English", "Português"});
        JLabel idiomas = new JLabel("Idioma / Language / Linguagem: ");
        idiomas.setForeground(textColor);
        panelTop.add(idiomas);
        panelTop.add(cmb_idiomas);
        JToggleButton btnToggleTheme = new JToggleButton("🌞");
        btnToggleTheme.setToolTipText("Cambiar tema claro/oscuro");
        btnToggleTheme.setFocusPainted(false);
        btnToggleTheme.setBackground(new Color(0,0,0,0)); // transparente
        panelTop.add(btnToggleTheme);
        this.btnToggleTheme = btnToggleTheme;
        contentPane.add(panelTop, BorderLayout.NORTH);

        // SplitPane central
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(300);
        splitPane.setDividerSize(5);
        splitPane.setBorder(null);
        contentPane.add(splitPane, BorderLayout.CENTER);

        // --- Panel izquierdo (formulario) ---
        JPanel panelIzquierdo = new JPanel(new BorderLayout(10, 10));
        panelIzquierdo.setBackground(panelColor);
        panelIzquierdo.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel panelFormulario = new JPanel(new GridLayout(0, 1, 5, 5));
        panelFormulario.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panelFormulario.setBackground(panelColor);

        lblNombre = crearLabel(""); txt_nombres = crearTextField();
        lblTelefono = crearLabel(""); txt_telefono = crearTextField();
        lblEmail = crearLabel(""); txt_email = crearTextField();
        lblCategoria = crearLabel(""); cmb_categoria = new JComboBox<>();
        lblFav = crearLabel(""); chb_favorito = new JCheckBox();
        chb_favorito.setOpaque(false);

        panelFormulario.add(lblNombre); panelFormulario.add(txt_nombres);
        panelFormulario.add(lblTelefono); panelFormulario.add(txt_telefono);
        panelFormulario.add(lblEmail); panelFormulario.add(txt_email);
        panelFormulario.add(lblCategoria); panelFormulario.add(cmb_categoria);
        panelFormulario.add(lblFav); panelFormulario.add(chb_favorito);

        JPanel panelBotonesCRUD = new JPanel(new GridLayout(3, 1, 5, 5));
        panelBotonesCRUD.setBackground(panelColor);
        btn_add = crearBoton("Agregar", primaryColor);
        btn_add.setIcon(obtenerIcono("/recursos/iconos/plus.png", 20, 20));
        btn_modificar = crearBoton("Modificar", primaryColor);
        btn_modificar.setIcon(obtenerIcono("/recursos/iconos/pencil.png", 20, 20));
        btn_eliminar = crearBoton("Eliminar", Color.decode("#005eb8"));
        btn_eliminar.setIcon(obtenerIcono("/recursos/iconos/cross.png", 20, 20));
        panelBotonesCRUD.add(btn_add);
        panelBotonesCRUD.add(btn_modificar);
        panelBotonesCRUD.add(btn_eliminar);

        panelIzquierdo.add(panelFormulario, BorderLayout.NORTH);
        panelIzquierdo.add(panelBotonesCRUD, BorderLayout.SOUTH);
        splitPane.setLeftComponent(panelIzquierdo);

        // --- Panel derecho ---
        JPanel panelDerecho = new JPanel(new BorderLayout(10, 10));
        panelDerecho.setBackground(bgColor);
        panelDerecho.setBorder(new EmptyBorder(0, 15, 0, 0));

        // Top: búsqueda + estadísticas
        JPanel panelDerechoTop = new JPanel(new BorderLayout());
        panelDerechoTop.setBackground(bgColor);
        JPanel panelBuscar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelBuscar.setBackground(bgColor);
        lblBuscar = crearLabel(""); txt_buscar = crearTextField();
        txt_buscar.setPreferredSize(new Dimension(200, 25));
        panelBuscar.add(lblBuscar); panelBuscar.add(txt_buscar);
        JPanel panelStats = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        panelStats.setBackground(bgColor);
        lbl_total = crearLabel(""); lbl_favoritos = crearLabel("");
        lbl_amigos = crearLabel(""); lbl_trabajo = crearLabel(""); lbl_familia = crearLabel("");
        panelStats.add(lbl_total); panelStats.add(lbl_favoritos);
        panelStats.add(lbl_amigos); panelStats.add(lbl_trabajo); panelStats.add(lbl_familia);
        panelDerechoTop.add(panelBuscar, BorderLayout.WEST);
        panelDerechoTop.add(panelStats, BorderLayout.EAST);

        // Tabla
        String[] columnas = {"Nombre", "Teléfono", "Email", "Categoría", "Favorito"};
        tableModel = new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tbl_contactos = new JTable(tableModel);
        tbl_contactos.getTableHeader().setReorderingAllowed(false);
        scrTabla = new JScrollPane(tbl_contactos);

        // Bottom: botones CSV + JSON y barra de progreso
        JPanel panelDerechoBottom = new JPanel(new BorderLayout(5, 5));
        panelDerechoBottom.setBackground(bgColor);
        JPanel panelArchivos = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelArchivos.setBackground(bgColor);
        // NUEVOS BOTONES JSON
        btn_exportarJson = crearBoton("Exportar JSON", primaryColor);
        btn_exportarJson.setIcon(obtenerIcono("/recursos/iconos/angle-up.png", 18, 18));
        btn_importarJson = crearBoton("Importar JSON", primaryColor);
        btn_importarJson.setIcon(obtenerIcono("/recursos/iconos/angle-down.png", 18, 18));
        panelArchivos.add(btn_exportarJson);
        panelArchivos.add(btn_importarJson);

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setValue(0);
        progressBar.setString("Listo");

        panelDerechoBottom.add(panelArchivos, BorderLayout.NORTH);
        panelDerechoBottom.add(progressBar, BorderLayout.SOUTH);

        panelDerecho.add(panelDerechoTop, BorderLayout.NORTH);
        panelDerecho.add(scrTabla, BorderLayout.CENTER);
        panelDerecho.add(panelDerechoBottom, BorderLayout.SOUTH);

        splitPane.setRightComponent(panelDerecho);

        new logica_ventana(this);
    }

    // Métodos auxiliares (crearLabel, crearTextField, crearBoton, obtenerIcono)...
    private JLabel crearLabel(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(textColor);
        return lbl;
    }

    private JTextField crearTextField() {
        JTextField txt = new JTextField();
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txt.setBackground(textBoxColor);
        txt.setForeground(Color.BLACK);
        txt.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));
        return txt;
    }

    private JButton crearBoton(String texto, Color bg) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        return btn;
    }

    private ImageIcon obtenerIcono(String rutaArchivo, int ancho, int alto) {
        try {
            java.net.URL imgUrl = getClass().getResource(rutaArchivo);
            if (imgUrl == null) {
                System.out.println("Ícono no encontrado: " + rutaArchivo);
                return null;
            }
            Image srcImg = new ImageIcon(imgUrl).getImage();
            BufferedImage resizedImg = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = resizedImg.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.drawImage(srcImg, 0, 0, ancho, alto, null);
            g2.dispose();
            return new ImageIcon(resizedImg);
        } catch (Exception e) {
            System.out.println("Error al procesar el ícono: " + e.getMessage());
            return null;
        }
    }

    public void aplicarIdioma(ResourceBundle rb) {
        lblNombre.setText(rb.getString("lbl.nombres"));
        lblTelefono.setText(rb.getString("lbl.telefono"));
        lblEmail.setText(rb.getString("lbl.email"));
        lblCategoria.setText(rb.getString("lbl.categoria"));
        lblFav.setText(rb.getString("lbl.favorito"));
        lblBuscar.setText(rb.getString("lbl.buscar"));
        chb_favorito.setText(rb.getString("lbl.marcar.fav"));

        btn_add.setText(rb.getString("btn.agregar"));
        btn_modificar.setText(rb.getString("btn.modificar"));
        btn_eliminar.setText(rb.getString("btn.eliminar"));
        // NUEVO: textos para botones JSON
        btn_exportarJson.setText(rb.getString("btn.exportar.json"));
        btn_importarJson.setText(rb.getString("btn.importar.json"));

        tbl_contactos.getColumnModel().getColumn(0).setHeaderValue(rb.getString("lbl.nombres"));
        tbl_contactos.getColumnModel().getColumn(1).setHeaderValue(rb.getString("lbl.telefono"));
        tbl_contactos.getColumnModel().getColumn(2).setHeaderValue(rb.getString("lbl.email"));
        tbl_contactos.getColumnModel().getColumn(3).setHeaderValue(rb.getString("lbl.categoria"));
        tbl_contactos.getColumnModel().getColumn(4).setHeaderValue(rb.getString("lbl.favorito"));
        tbl_contactos.getTableHeader().repaint();

        int idx = cmb_categoria.getSelectedIndex();
        cmb_categoria.removeAllItems();
        cmb_categoria.addItem(rb.getString("cat.amigo"));
        cmb_categoria.addItem(rb.getString("cat.trabajo"));
        cmb_categoria.addItem(rb.getString("cat.familia"));
        cmb_categoria.addItem(rb.getString("cat.otro"));
        if (idx >= 0) cmb_categoria.setSelectedIndex(idx);
    }
}