package com.securboration.swing;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.securboration.demo.ImageProcessingBenchmark;
import com.securboration.demo.kernel.ConvolutionConfigurations;
import com.securboration.demo.kernel.DoNothingKernel;
import com.securboration.demo.kernel.IImageProcessingKernel;

/**
 * Swing frame that allows a user to select which operation should be applied to
 * a stream of input images
 * 
 * @author jstaples
 *
 */
public class KernelSelector extends JFrame {
    private static final long serialVersionUID = 1L;

    private final Map<String, IImageProcessingKernel> kernels = new TreeMap<>();

    private JPanel contentPane;

    /**
     * Create the frame.
     */
    public KernelSelector(final ImageProcessingBenchmark benchmark) {
        kernels.put("[none]", new DoNothingKernel());

        for (ConvolutionConfigurations c : ConvolutionConfigurations.values()) {
            kernels.put(c.toString() + " naive", c.getNaiveConvolver());

            kernels.put(c.toString() + " efficient", c.getEfficientConvolver());
        }

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 244, 73);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);

        JComboBox<String> comboBox = new JComboBox<String>(
                kernels.keySet().toArray(new String[] {}));
        contentPane.add(comboBox, BorderLayout.CENTER);

        comboBox.addActionListener((ActionEvent e) -> {
            IImageProcessingKernel kernel = kernels
                    .get(comboBox.getSelectedItem());

            benchmark.setKernel(kernel);
        });
    }

}
