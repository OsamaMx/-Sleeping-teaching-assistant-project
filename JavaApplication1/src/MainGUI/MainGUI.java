package MainGUI;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class MainGUI {
    private JFrame frame;
    private JTextField tfTAs;
    private JTextField tfChairs;
    private JTextField tfStudents;
    private JFrame outputFrame;
    private JTextArea outputTextArea;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                MainGUI window = new MainGUI();
                window.frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public MainGUI() {
        initialize();
    }

    private void initialize() {
        frame = new JFrame();
        frame.setBounds(100, 100, 450, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new GridLayout(4, 2, 10, 10));

        JLabel lblTAs = new JLabel("Number of TAs:");
        tfTAs = new JTextField();
        JLabel lblChairs = new JLabel("Number of Chairs:");
        tfChairs = new JTextField();
        JLabel lblStudents = new JLabel("Number of Students:");
        tfStudents = new JTextField();

        JButton btnStart = new JButton("Start Simulation");

        frame.getContentPane().add(lblTAs);
        frame.getContentPane().add(tfTAs);
        frame.getContentPane().add(lblChairs);
        frame.getContentPane().add(tfChairs);
        frame.getContentPane().add(lblStudents);
        frame.getContentPane().add(tfStudents);
        frame.getContentPane().add(btnStart);

        btnStart.addActionListener(e -> startSimulation());
    }

    private void startSimulation() {
        int number_of_TAs = Integer.parseInt(tfTAs.getText());
        int number_of_Chairs = Integer.parseInt(tfChairs.getText());
        int number_of_Students = Integer.parseInt(tfStudents.getText());

        // Create a new window for output
        outputFrame = new JFrame();
        outputFrame.setBounds(100, 100, 600, 400);
        outputFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Create a JTextArea for output
        outputTextArea = new JTextArea();
        outputTextArea.setEditable(false);

        // Add the JTextArea to a JScrollPane for scrolling
        JScrollPane scrollPane = new JScrollPane(outputTextArea);
        outputFrame.getContentPane().add(scrollPane);

        // Make the output window visible
        outputFrame.setVisible(true);

        // Call your runSimulation function with the provided values
        runSimulation(number_of_TAs, number_of_Chairs, number_of_Students);
    }

    private void runSimulation(int number_of_TAs, int number_of_Chairs, int number_of_Students) {
        // Redirect System.out to JTextArea
        PrintStream printStream = new PrintStream(new CustomOutputStream(outputTextArea));
        System.setOut(printStream);

        AtomicInteger Counter = new AtomicInteger(0);
        AtomicInteger acutal_number = new AtomicInteger(0);

        Semaphore v = new Semaphore(1);
        Semaphore TAs = new Semaphore(0);
        Semaphore Chairs = new Semaphore(0);

        // Creating a thread pool
        ExecutorService executor = Executors.newFixedThreadPool(number_of_Students + number_of_TAs);
   
        // Students section
        for (int i = 0; i < number_of_Students; i++) {
            executor.execute(() -> {
                try {
                   
                    if (TAs.tryAcquire()) {

                    } else {
                        if (!acutal_number.compareAndSet(number_of_Chairs + number_of_TAs, acutal_number.get())) {
                            acutal_number.incrementAndGet();
                            Chairs.release();
                             v.acquire();
                    System.out.println("working  " + ( number_of_TAs- TAs.availablePermits()) + " TAs ");
                    System.out.println("Sleeping  " + (TAs.availablePermits()) + " TAs ");
                    if(number_of_TAs> number_of_Students){
                         System.out.println(0+ " Chairs ");
                    }
                    else{
                        System.out.println((acutal_number.get()-number_of_TAs)+ " Chairs ");
                    }
                    System.out.println(Counter.get() + " Students left ");
                   System.out.println("__________");
                    v.release();
                            TAs.acquire();
                            acutal_number.decrementAndGet();

                        } else {
                            Counter.incrementAndGet();
                         System.out.println(Counter.get() + " Students left ");
                        }
                    }
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            });
        }

        // TAs section
     for (int i = 0; i < number_of_TAs; i++) {
            executor.execute(() -> {

                try {
                    v.acquire();
                    System.out.println("working  " + ( number_of_TAs- TAs.availablePermits()) + " TAs ");
                    System.out.println("Sleeping  " + (TAs.availablePermits()) + " TAs ");
                    if(number_of_TAs> number_of_Students){
                        System.out.println(0+ " Chairs ");
                    }
                    else{
                        System.out.println((acutal_number.get()-number_of_TAs)+ " Chairs ");
                    }
                    
                   System.out.println(Counter.get() + " Students left ");
                    System.out.println("__________");
                    v.release();
                    TAs.release();
                    Chairs.acquire();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            });
        }

        // Shutdown the thread pool
        executor.shutdown();
  }

    // Custom output stream to redirect System.out to JTextArea
    private class CustomOutputStream extends OutputStream {
        private JTextArea textArea;

        public CustomOutputStream(JTextArea textArea) {
            this.textArea = textArea;
        }

        public void write(int b) throws IOException {
            textArea.append(String.valueOf((char) b));
            textArea.setCaretPosition(textArea.getDocument().getLength());
        }
    }
}