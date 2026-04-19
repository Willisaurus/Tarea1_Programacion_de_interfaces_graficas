package vista;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
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
    public JTabbedPane tabbedPane;
    public JProgressBar progressBar;

    public JLabel lbl_total;
    public JLabel lbl_favoritos;
    public JLabel lbl_amigos;
    public JLabel lbl_trabajo;
    public JLabel lbl_familia;

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

    public ventana() {
        setTitle("GESTIÓN DE CONTACTOS - MVC");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setBounds(100, 100, 1050, 730);

        /*
         * ELIMINADO DEL CÓDIGO ANTERIOR:
         * contentPane = new JPanel();
         * contentPane.setLayout(null);
         * (y luego setBounds manual para cada JLabel, JTextField, JButton, etc.)
         *
         * MOTIVO:
         * Se reemplaza el posicionamiento absoluto por Layout Managers
         * para una interfaz más ordenada, mantenible y escalable.
         */

        contentPane = new JPanel(new BorderLayout());
        contentPane.setBorder(new EmptyBorder(8, 8, 8, 8));
        setContentPane(contentPane);

        // NUEVO REQ-1: pestañas
        tabbedPane = new JTabbedPane();
        contentPane.add(tabbedPane, BorderLayout.CENTER);

        // TAB CONTACTOS
        JPanel panelContactos = new JPanel(new BorderLayout(8, 8));
        tabbedPane.addTab("Contactos", panelContactos);

        JPanel panelFormulario = new JPanel(new GridLayout(3, 4, 8, 8));

        JLabel lblNombre = new JLabel("NOMBRES:");
        lblNombre.setFont(new Font("Tahoma", Font.BOLD, 12));
        panelFormulario.add(lblNombre);

        txt_nombres = new JTextField();
        panelFormulario.add(txt_nombres);

        JLabel lblTelefono = new JLabel("TELÉFONO:");
        lblTelefono.setFont(new Font("Tahoma", Font.BOLD, 12));
        panelFormulario.add(lblTelefono);

        txt_telefono = new JTextField();
        panelFormulario.add(txt_telefono);

        JLabel lblEmail = new JLabel("EMAIL:");
        lblEmail.setFont(new Font("Tahoma", Font.BOLD, 12));
        panelFormulario.add(lblEmail);

        txt_email = new JTextField();
        panelFormulario.add(txt_email);

        JLabel lblCategoria = new JLabel("CATEGORÍA:");
        lblCategoria.setFont(new Font("Tahoma", Font.BOLD, 12));
        panelFormulario.add(lblCategoria);

        cmb_categoria = new JComboBox<>(new String[]{"Amigo", "Trabajo", "Familia", "Otro"});
        panelFormulario.add(cmb_categoria);

        JLabel lblFav = new JLabel("FAVORITO:");
        lblFav.setFont(new Font("Tahoma", Font.BOLD, 12));
        panelFormulario.add(lblFav);

        chb_favorito = new JCheckBox("Marcar como favorito");
        panelFormulario.add(chb_favorito);

        JLabel lblBuscar = new JLabel("BUSCAR:");
        lblBuscar.setFont(new Font("Tahoma", Font.BOLD, 12));
        panelFormulario.add(lblBuscar);

        txt_buscar = new JTextField();
        panelFormulario.add(txt_buscar);

        panelContactos.add(panelFormulario, BorderLayout.NORTH);

        // NUEVO REQ-3: tabla
        String[] columnas = {"Nombre", "Teléfono", "Email", "Categoría", "Favorito"};
        tableModel = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tbl_contactos = new JTable(tableModel);
        tbl_contactos.getTableHeader().setReorderingAllowed(false);
        scrTabla = new JScrollPane(tbl_contactos);
        panelContactos.add(scrTabla, BorderLayout.CENTER);

        JPanel panelInferior = new JPanel(new BorderLayout());

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        btn_add = new JButton("Agregar");
        btn_modificar = new JButton("Modificar");
        btn_eliminar = new JButton("Eliminar");
        btn_exportar = new JButton("Exportar CSV"); // NUEVO
        btn_importar = new JButton("Importar CSV");
        panelBotones.add(btn_add);
        panelBotones.add(btn_modificar);
        panelBotones.add(btn_eliminar);
        panelBotones.add(btn_exportar);
        panelBotones.add(btn_importar);

        panelInferior.add(panelBotones, BorderLayout.WEST);

        // NUEVO REQ-3: barra progreso
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setValue(0);
        progressBar.setString("Listo");
        panelInferior.add(progressBar, BorderLayout.SOUTH);

        panelContactos.add(panelInferior, BorderLayout.SOUTH);

        // TAB ESTADÍSTICAS
        JPanel panelStats = new JPanel(new GridLayout(5, 1, 8, 8));
        tabbedPane.addTab("Estadísticas", panelStats);

        lbl_total = new JLabel("Total contactos: 0");
        lbl_favoritos = new JLabel("Favoritos: 0");
        lbl_amigos = new JLabel("Categoría Amigo: 0");
        lbl_trabajo = new JLabel("Categoría Trabajo: 0");
        lbl_familia = new JLabel("Categoría Familia: 0");

        panelStats.add(lbl_total);
        panelStats.add(lbl_favoritos);
        panelStats.add(lbl_amigos);
        panelStats.add(lbl_trabajo);
        panelStats.add(lbl_familia);

        // Conexión MVC
        new logica_ventana(this);
    }
}