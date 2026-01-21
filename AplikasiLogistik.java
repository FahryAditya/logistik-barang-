import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.text.SimpleDateFormat;

public class AplikasiLogistik extends JFrame {
    private JTable tableBarang, tableTransaksi;
    private DefaultTableModel modelBarang, modelTransaksi;
    private JTextField txtKode, txtNama, txtStok, txtHarga;
    private JTextField txtKodeTransaksi, txtJumlahTransaksi;
    private JComboBox<String> cmbJenisTransaksi;
    private ArrayList<Barang> daftarBarang;
    private ArrayList<Transaksi> daftarTransaksi;
    
    public AplikasiLogistik() {
        daftarBarang = new ArrayList<>();
        daftarTransaksi = new ArrayList<>();
        
        setTitle("Sistem Manajemen Logistik Barang");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        initComponents();
    }
    
    private void initComponents() {
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Tab Master Barang
        JPanel panelBarang = createPanelBarang();
        tabbedPane.addTab("Master Barang", panelBarang);
        
        // Tab Transaksi
        JPanel panelTransaksi = createPanelTransaksi();
        tabbedPane.addTab("Transaksi Keluar-Masuk", panelTransaksi);
        
        // Tab Laporan
        JPanel panelLaporan = createPanelLaporan();
        tabbedPane.addTab("Laporan", panelLaporan);
        
        add(tabbedPane);
    }
    
    private JPanel createPanelBarang() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Panel Input
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("Data Barang"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Kode Barang
        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("Kode Barang:"), gbc);
        gbc.gridx = 1;
        txtKode = new JTextField(15);
        inputPanel.add(txtKode, gbc);
        
        // Nama Barang
        gbc.gridx = 0; gbc.gridy = 1;
        inputPanel.add(new JLabel("Nama Barang:"), gbc);
        gbc.gridx = 1;
        txtNama = new JTextField(15);
        inputPanel.add(txtNama, gbc);
        
        // Stok Awal
        gbc.gridx = 0; gbc.gridy = 2;
        inputPanel.add(new JLabel("Stok Awal:"), gbc);
        gbc.gridx = 1;
        txtStok = new JTextField(15);
        inputPanel.add(txtStok, gbc);
        
        // Harga
        gbc.gridx = 0; gbc.gridy = 3;
        inputPanel.add(new JLabel("Harga Satuan:"), gbc);
        gbc.gridx = 1;
        txtHarga = new JTextField(15);
        inputPanel.add(txtHarga, gbc);
        
        // Tombol
        JPanel btnPanel = new JPanel(new FlowLayout());
        JButton btnTambah = new JButton("Tambah");
        JButton btnUpdate = new JButton("Update");
        JButton btnHapus = new JButton("Hapus");
        JButton btnBersih = new JButton("Bersihkan");
        
        btnTambah.addActionListener(e -> tambahBarang());
        btnUpdate.addActionListener(e -> updateBarang());
        btnHapus.addActionListener(e -> hapusBarang());
        btnBersih.addActionListener(e -> bersihkanForm());
        
        btnPanel.add(btnTambah);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnHapus);
        btnPanel.add(btnBersih);
        
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        inputPanel.add(btnPanel, gbc);
        
        // Tabel Barang
        String[] columnNames = {"Kode", "Nama Barang", "Stok", "Harga", "Total Nilai"};
        modelBarang = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableBarang = new JTable(modelBarang);
        tableBarang.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tableBarang.getSelectedRow() != -1) {
                int row = tableBarang.getSelectedRow();
                txtKode.setText(tableBarang.getValueAt(row, 0).toString());
                txtNama.setText(tableBarang.getValueAt(row, 1).toString());
                txtStok.setText(tableBarang.getValueAt(row, 2).toString());
                txtHarga.setText(tableBarang.getValueAt(row, 3).toString());
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(tableBarang);
        
        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createPanelTransaksi() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Panel Input Transaksi
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("Input Transaksi"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("Kode Barang:"), gbc);
        gbc.gridx = 1;
        txtKodeTransaksi = new JTextField(15);
        inputPanel.add(txtKodeTransaksi, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        inputPanel.add(new JLabel("Jenis Transaksi:"), gbc);
        gbc.gridx = 1;
        cmbJenisTransaksi = new JComboBox<>(new String[]{"Masuk", "Keluar"});
        inputPanel.add(cmbJenisTransaksi, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        inputPanel.add(new JLabel("Jumlah:"), gbc);
        gbc.gridx = 1;
        txtJumlahTransaksi = new JTextField(15);
        inputPanel.add(txtJumlahTransaksi, gbc);
        
        JPanel btnPanel = new JPanel(new FlowLayout());
        JButton btnProses = new JButton("Proses Transaksi");
        btnProses.addActionListener(e -> prosesTransaksi());
        btnPanel.add(btnProses);
        
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        inputPanel.add(btnPanel, gbc);
        
        // Tabel Transaksi
        String[] columnNames = {"Tanggal", "Kode Barang", "Nama Barang", "Jenis", "Jumlah", "Stok Akhir"};
        modelTransaksi = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableTransaksi = new JTable(modelTransaksi);
        JScrollPane scrollPane = new JScrollPane(tableTransaksi);
        
        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createPanelLaporan() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JTextArea txtLaporan = new JTextArea();
        txtLaporan.setEditable(false);
        txtLaporan.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(txtLaporan);
        
        JButton btnGenerate = new JButton("Generate Laporan");
        btnGenerate.addActionListener(e -> {
            StringBuilder laporan = new StringBuilder();
            laporan.append("=".repeat(80)).append("\n");
            laporan.append("                    LAPORAN STOK BARANG\n");
            laporan.append("=".repeat(80)).append("\n\n");
            
            laporan.append(String.format("%-15s %-25s %10s %15s %20s\n", 
                "Kode", "Nama Barang", "Stok", "Harga", "Total Nilai"));
            laporan.append("-".repeat(80)).append("\n");
            
            double totalNilai = 0;
            for (Barang b : daftarBarang) {
                double nilai = b.getStok() * b.getHarga();
                totalNilai += nilai;
                laporan.append(String.format("%-15s %-25s %10d %15.2f %20.2f\n",
                    b.getKode(), b.getNama(), b.getStok(), b.getHarga(), nilai));
            }
            
            laporan.append("-".repeat(80)).append("\n");
            laporan.append(String.format("%-51s TOTAL: %20.2f\n", "", totalNilai));
            laporan.append("=".repeat(80)).append("\n\n");
            
            laporan.append("                    RIWAYAT TRANSAKSI\n");
            laporan.append("=".repeat(80)).append("\n");
            laporan.append(String.format("%-20s %-15s %-10s %10s %15s\n",
                "Tanggal", "Kode", "Jenis", "Jumlah", "Stok Akhir"));
            laporan.append("-".repeat(80)).append("\n");
            
            for (Transaksi t : daftarTransaksi) {
                laporan.append(String.format("%-20s %-15s %-10s %10d %15d\n",
                    t.getTanggal(), t.getKodeBarang(), t.getJenis(), 
                    t.getJumlah(), t.getStokAkhir()));
            }
            laporan.append("=".repeat(80)).append("\n");
            
            txtLaporan.setText(laporan.toString());
        });
        
        panel.add(btnGenerate, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void tambahBarang() {
        try {
            String kode = txtKode.getText().trim();
            String nama = txtNama.getText().trim();
            int stok = Integer.parseInt(txtStok.getText().trim());
            double harga = Double.parseDouble(txtHarga.getText().trim());
            
            if (kode.isEmpty() || nama.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Kode dan Nama tidak boleh kosong!");
                return;
            }
            
            // Cek duplikasi
            for (Barang b : daftarBarang) {
                if (b.getKode().equals(kode)) {
                    JOptionPane.showMessageDialog(this, "Kode barang sudah ada!");
                    return;
                }
            }
            
            Barang barang = new Barang(kode, nama, stok, harga);
            daftarBarang.add(barang);
            refreshTableBarang();
            bersihkanForm();
            JOptionPane.showMessageDialog(this, "Barang berhasil ditambahkan!");
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Stok dan Harga harus berupa angka!");
        }
    }
    
    private void updateBarang() {
        try {
            String kode = txtKode.getText().trim();
            for (Barang b : daftarBarang) {
                if (b.getKode().equals(kode)) {
                    b.setNama(txtNama.getText().trim());
                    b.setStok(Integer.parseInt(txtStok.getText().trim()));
                    b.setHarga(Double.parseDouble(txtHarga.getText().trim()));
                    refreshTableBarang();
                    bersihkanForm();
                    JOptionPane.showMessageDialog(this, "Barang berhasil diupdate!");
                    return;
                }
            }
            JOptionPane.showMessageDialog(this, "Barang tidak ditemukan!");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Stok dan Harga harus berupa angka!");
        }
    }
    
    private void hapusBarang() {
        int row = tableBarang.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Pilih barang yang akan dihapus!");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Yakin ingin menghapus barang ini?", "Konfirmasi", 
            JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            String kode = tableBarang.getValueAt(row, 0).toString();
            daftarBarang.removeIf(b -> b.getKode().equals(kode));
            refreshTableBarang();
            bersihkanForm();
            JOptionPane.showMessageDialog(this, "Barang berhasil dihapus!");
        }
    }
    
    private void prosesTransaksi() {
        try {
            String kode = txtKodeTransaksi.getText().trim();
            String jenis = cmbJenisTransaksi.getSelectedItem().toString();
            int jumlah = Integer.parseInt(txtJumlahTransaksi.getText().trim());
            
            Barang barang = null;
            for (Barang b : daftarBarang) {
                if (b.getKode().equals(kode)) {
                    barang = b;
                    break;
                }
            }
            
            if (barang == null) {
                JOptionPane.showMessageDialog(this, "Barang tidak ditemukan!");
                return;
            }
            
            if (jenis.equals("Keluar")) {
                if (barang.getStok() < jumlah) {
                    JOptionPane.showMessageDialog(this, "Stok tidak mencukupi!");
                    return;
                }
                barang.setStok(barang.getStok() - jumlah);
            } else {
                barang.setStok(barang.getStok() + jumlah);
            }
            
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            String tanggal = sdf.format(new Date());
            
            Transaksi transaksi = new Transaksi(tanggal, kode, barang.getNama(), 
                jenis, jumlah, barang.getStok());
            daftarTransaksi.add(transaksi);
            
            refreshTableBarang();
            refreshTableTransaksi();
            
            txtKodeTransaksi.setText("");
            txtJumlahTransaksi.setText("");
            
            JOptionPane.showMessageDialog(this, "Transaksi berhasil diproses!");
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Jumlah harus berupa angka!");
        }
    }
    
    private void bersihkanForm() {
        txtKode.setText("");
        txtNama.setText("");
        txtStok.setText("");
        txtHarga.setText("");
    }
    
    private void refreshTableBarang() {
        modelBarang.setRowCount(0);
        for (Barang b : daftarBarang) {
            Object[] row = {
                b.getKode(),
                b.getNama(),
                b.getStok(),
                String.format("%.2f", b.getHarga()),
                String.format("%.2f", b.getStok() * b.getHarga())
            };
            modelBarang.addRow(row);
        }
    }
    
    private void refreshTableTransaksi() {
        modelTransaksi.setRowCount(0);
        for (Transaksi t : daftarTransaksi) {
            Object[] row = {
                t.getTanggal(),
                t.getKodeBarang(),
                t.getNamaBarang(),
                t.getJenis(),
                t.getJumlah(),
                t.getStokAkhir()
            };
            modelTransaksi.addRow(row);
        }
    }
    
    // Inner Class Barang
    class Barang {
        private String kode;
        private String nama;
        private int stok;
        private double harga;
        
        public Barang(String kode, String nama, int stok, double harga) {
            this.kode = kode;
            this.nama = nama;
            this.stok = stok;
            this.harga = harga;
        }
        
        public String getKode() { return kode; }
        public String getNama() { return nama; }
        public int getStok() { return stok; }
        public double getHarga() { return harga; }
        
        public void setNama(String nama) { this.nama = nama; }
        public void setStok(int stok) { this.stok = stok; }
        public void setHarga(double harga) { this.harga = harga; }
    }
    
    // Inner Class Transaksi
    class Transaksi {
        private String tanggal;
        private String kodeBarang;
        private String namaBarang;
        private String jenis;
        private int jumlah;
        private int stokAkhir;
        
        public Transaksi(String tanggal, String kodeBarang, String namaBarang, 
                        String jenis, int jumlah, int stokAkhir) {
            this.tanggal = tanggal;
            this.kodeBarang = kodeBarang;
            this.namaBarang = namaBarang;
            this.jenis = jenis;
            this.jumlah = jumlah;
            this.stokAkhir = stokAkhir;
        }
        
        public String getTanggal() { return tanggal; }
        public String getKodeBarang() { return kodeBarang; }
        public String getNamaBarang() { return namaBarang; }
        public String getJenis() { return jenis; }
        public int getJumlah() { return jumlah; }
        public int getStokAkhir() { return stokAkhir; }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new AplikasiLogistik().setVisible(true);
        });
    }
}
