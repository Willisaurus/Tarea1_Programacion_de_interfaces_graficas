package vista;
//Se importan nuevas clases para poder crear los metodos auxiliares
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ResourceBundle;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import controlador.logica_ventana;

public class ventana extends JFrame {

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
    public JButton btn_importar;

    // NUEVO
    public JButton btn_exportar;
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
    //Combo box para seleccionar idioma
    public JComboBox<String> cmb_idiomas;
    //Etiquetas de formulario para i18n
    private JLabel lblNombre;
    private JLabel lblTelefono;
    private JLabel lblEmail;
    private JLabel lblCategoria;
    private JLabel lblFav;
    private JLabel lblBuscar;

    // Paleta de colores
    Color bgColor = Color.decode("#53739A");
    Color panelColor = Color.decode("#6E8CB1");
    Color primaryColor = Color.decode("#3A486B");
    Color textBoxColor = Color.decode("#9EB2CB");
    Color textColor = Color.WHITE;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                ventana frame = new ventana();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    /*
    A priori de un diseno mas enfocado al usuario, comodo, minimalista
    y atrativo se elimino toda la distribucion anterior del constructor
    pasando de TabbedPane a SplitPane que usa el patron de diseno master
    detail
     */
    public ventana() {
        setTitle("GESTIÓN DE CONTACTOS - MVC");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);
        setBounds(100, 100, 1050, 730);

        contentPane = new JPanel(new BorderLayout());
        contentPane.setBackground(bgColor);
        contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        setContentPane(contentPane);

        // ==========================================
        // 1. BARRA SUPERIOR (Idiomas)
        // ==========================================
        JPanel panelTop = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelTop.setBackground(bgColor);
        cmb_idiomas = new JComboBox<>(new String[]{"Español", "English", "Português"});
        JLabel idiomas = new JLabel("Idioma / Language / Linguagem: ");
        idiomas.setForeground(textColor);
        panelTop.add(idiomas);
        panelTop.add(cmb_idiomas);
        contentPane.add(panelTop, BorderLayout.NORTH);

        // ==========================================
        // 2. CREACIÓN DEL SPLIT PANE (El divisor central)
        // ==========================================
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(300); // Ancho inicial del panel izquierdo
        splitPane.setDividerSize(5); // Grosor de la línea divisoria
        splitPane.setBorder(null);
        contentPane.add(splitPane, BorderLayout.CENTER);

        // ==========================================
        // 3. PANEL IZQUIERDO (Formulario y Botones CRUD)
        // ==========================================
        // Usamos BorderLayout: El formulario arriba (North) y los botones abajo (South)
        JPanel panelIzquierdo = new JPanel(new BorderLayout(10, 10));
        panelIzquierdo.setBackground(panelColor);
        panelIzquierdo.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Formulario vertical usando GridLayout(0, 1) -> 1 sola columna, filas infinitas
        JPanel panelFormulario = new JPanel(new GridLayout(0, 1, 5, 5));
        panelFormulario.setBackground(panelColor);

        lblNombre = crearLabel(""); txt_nombres = crearTextField();
        lblTelefono = crearLabel(""); txt_telefono = crearTextField();
        lblEmail = crearLabel(""); txt_email = crearTextField();
        lblCategoria = crearLabel(""); cmb_categoria = new JComboBox<>();
        lblFav = crearLabel(""); chb_favorito = new JCheckBox();
        chb_favorito.setOpaque(false); // Transparencia aplicada

        panelFormulario.add(lblNombre); panelFormulario.add(txt_nombres);
        panelFormulario.add(lblTelefono); panelFormulario.add(txt_telefono);
        panelFormulario.add(lblEmail); panelFormulario.add(txt_email);
        panelFormulario.add(lblCategoria); panelFormulario.add(cmb_categoria);
        panelFormulario.add(lblFav); panelFormulario.add(chb_favorito);

        // Botones debajo del formulario (Verticales también)
        JPanel panelBotonesCRUD = new JPanel(new GridLayout(3, 1, 5, 5));
        panelBotonesCRUD.setBackground(panelColor);
        btn_add = crearBoton("Agregar", primaryColor);
        //Agregamos los iconos a los botones
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

        // Asignamos el panel izquierdo al SplitPane
        splitPane.setLeftComponent(panelIzquierdo);

        // ==========================================
        // 4. PANEL DERECHO (Búsqueda, Tabla, Estadísticas y Archivos)
        // ==========================================
        JPanel panelDerecho = new JPanel(new BorderLayout(10, 10));
        panelDerecho.setBackground(bgColor);
        panelDerecho.setBorder(new EmptyBorder(0, 15, 0, 0)); // Margen solo a la izquierda

        // --- Cabecera del Panel Derecho (Buscar, Estadísticas, Exportar/Importar) ---
        JPanel panelDerechoTop = new JPanel(new BorderLayout());
        panelDerechoTop.setBackground(bgColor);

        // Zona de Búsqueda
        JPanel panelBuscar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelBuscar.setBackground(bgColor);
        lblBuscar = crearLabel(""); txt_buscar = crearTextField();
        txt_buscar.setPreferredSize(new Dimension(200, 25)); // Caja de búsqueda más larga
        panelBuscar.add(lblBuscar); panelBuscar.add(txt_buscar);

        // Zona de Estadísticas (Todo en una línea horizontal)
        JPanel panelStats = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        panelStats.setBackground(bgColor);
        lbl_total = crearLabel(""); lbl_favoritos = crearLabel("");
        lbl_amigos = crearLabel(""); lbl_trabajo = crearLabel(""); lbl_familia = crearLabel("");
        panelStats.add(lbl_total); panelStats.add(lbl_favoritos); panelStats.add(lbl_amigos);

        panelDerechoTop.add(panelBuscar, BorderLayout.WEST);
        panelDerechoTop.add(panelStats, BorderLayout.EAST);

        // --- Zona Central (Tabla) ---
        String[] columnas = {"Nombre", "Teléfono", "Email", "Categoría", "Favorito"};
        tableModel = new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tbl_contactos = new JTable(tableModel);
        tbl_contactos.getTableHeader().setReorderingAllowed(false);
        scrTabla = new JScrollPane(tbl_contactos);

        // --- Zona Inferior (Botones CSV y Progress Bar) ---
        JPanel panelDerechoBottom = new JPanel(new BorderLayout(5, 5));
        panelDerechoBottom.setBackground(bgColor);

        JPanel panelArchivos = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelArchivos.setBackground(bgColor);
        btn_exportar = crearBoton("Exportar CSV", primaryColor);
        btn_exportar.setIcon(obtenerIcono("/recursos/iconos/angle-down.png", 18, 18));
        btn_importar = crearBoton("Importar CSV", primaryColor);
        btn_importar.setIcon(obtenerIcono("/recursos/iconos/angle-up.png", 18, 18));
        panelArchivos.add(btn_exportar);
        panelArchivos.add(btn_importar);

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setValue(0);
        progressBar.setString("Listo");

        panelDerechoBottom.add(panelArchivos, BorderLayout.NORTH);
        panelDerechoBottom.add(progressBar, BorderLayout.SOUTH);

        // Ensamblamos el Panel Derecho
        panelDerecho.add(panelDerechoTop, BorderLayout.NORTH);
        panelDerecho.add(scrTabla, BorderLayout.CENTER);
        panelDerecho.add(panelDerechoBottom, BorderLayout.SOUTH);

        // Asignamos el panel derecho al SplitPane
        splitPane.setRightComponent(panelDerecho);

        new logica_ventana(this);
    }
    //Metodos auxiliares para evitar repetir codigo DRY
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

    //Metodo para poder aplicar idiomas
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
        btn_exportar.setText(rb.getString("btn.exportar"));
        btn_importar.setText(rb.getString("btn.importar"));

        // Refrescar nombres de columnas de la tabla
        tbl_contactos.getColumnModel().getColumn(0).setHeaderValue(rb.getString("lbl.nombres"));
        tbl_contactos.getColumnModel().getColumn(1).setHeaderValue(rb.getString("lbl.telefono"));
        tbl_contactos.getColumnModel().getColumn(2).setHeaderValue(rb.getString("lbl.email"));
        tbl_contactos.getColumnModel().getColumn(3).setHeaderValue(rb.getString("lbl.categoria"));
        tbl_contactos.getColumnModel().getColumn(4).setHeaderValue(rb.getString("lbl.favorito"));
        tbl_contactos.getTableHeader().repaint();

        // Actualizar combo box (guardando el índice seleccionado actual)
        int idx = cmb_categoria.getSelectedIndex();
        cmb_categoria.removeAllItems();
        cmb_categoria.addItem(rb.getString("cat.amigo"));
        cmb_categoria.addItem(rb.getString("cat.trabajo"));
        cmb_categoria.addItem(rb.getString("cat.familia"));
        cmb_categoria.addItem(rb.getString("cat.otro"));
        if(idx >= 0) cmb_categoria.setSelectedIndex(idx);
    }

    //Metodos para poder agregar los iconos respectivos
    private ImageIcon obtenerIcono(String rutaArchivo, int ancho, int alto) {
        try {
            java.net.URL imgUrl = getClass().getResource(rutaArchivo);
            if (imgUrl == null) {
                System.out.println("Ícono no encontrado: " + rutaArchivo);
                return null;
            }

            Image srcImg = new ImageIcon(imgUrl).getImage();

            // Usamos esta clase de java apra que ele scalado de los iconos sea mejor
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

}