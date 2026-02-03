package com.python.deep.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.annotation.PostConstruct;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Controller
public class DeepLearningController {

    private File getPythonDirectory() {
        String userDir = System.getProperty("user.dir");
        File pythonDir = new File(userDir, "src/main/resources/static/python");
        
        if (!pythonDir.exists()) {
             // Try inside springboot folder (if run from parent)
             pythonDir = new File(userDir, "springboot/src/main/resources/static/python");
        }
        return pythonDir;
    }

    @PostConstruct
    public void init() {
        try {
            File pythonDir = getPythonDirectory();
            File requirements = new File(pythonDir, "requirements.txt");
            
            if (requirements.exists()) {
                System.out.println("Installing Python dependencies from " + requirements.getAbsolutePath());
                List<String> command = new ArrayList<>();
                command.add("python");
                command.add("-m");
                command.add("pip");
                command.add("install");
                command.add("-r");
                command.add("requirements.txt");
                
                ProcessBuilder pb = new ProcessBuilder(command);
                pb.directory(pythonDir);
                pb.redirectErrorStream(true);
                Process process = pb.start();
                
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("[PIP] " + line);
                }
                
                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    System.out.println("Python dependencies installed successfully.");
                } else {
                    System.err.println("Failed to install Python dependencies. Exit code: " + exitCode);
                }
            } else {
                System.out.println("No requirements.txt found in " + pythonDir.getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/deep/diabetes")
    public String diabetesForm() {
        return "deep/diabetes";
    }
    
    @GetMapping("/deep/iris")
    public String irisForm() {
        return "deep/iris";
    }

    @PostMapping("/deep/iris/predict")
    public String irisPredict(
            @RequestParam("sepalLength") double sepalLength,
            @RequestParam("sepalWidth") double sepalWidth,
            @RequestParam("petalLength") double petalLength,
            @RequestParam("petalWidth") double petalWidth,
            Model model) {

        try {
            // Python script path
            String pythonScriptPath = "iris_deep.py";
            
            // Prepare command arguments
            List<String> command = new ArrayList<>();
            command.add("python");
            command.add(pythonScriptPath);
            command.add(String.valueOf(sepalLength));
            command.add(String.valueOf(sepalWidth));
            command.add(String.valueOf(petalLength));
            command.add(String.valueOf(petalWidth));

            ProcessBuilder pb = new ProcessBuilder(command);
            
            // Set working directory
            File pythonDir = getPythonDirectory();
            
            if (pythonDir.exists()) {
                pb.directory(pythonDir);
            }
            
            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
            
            String line;
            StringBuilder output = new StringBuilder();
            
            while ((line = reader.readLine()) != null) {
                // Filter output to show only results
                if (line.contains("예측 결과:") || line.contains("확률:")) {
                    output.append(line).append("\n");
                }
            }
            
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                output.append("Process exited with code ").append(exitCode);
            }

            model.addAttribute("result", output.toString());
            
            // Pass back input values
            model.addAttribute("sepalLength", sepalLength);
            model.addAttribute("sepalWidth", sepalWidth);
            model.addAttribute("petalLength", petalLength);
            model.addAttribute("petalWidth", petalWidth);

        } catch (Exception e) {
            model.addAttribute("result", "Error: " + e.getMessage());
        }

        return "deep/iris";
    }

    @PostMapping("/deep/diabetes/predict")
    public String diabetesPredict(
            @RequestParam("pregnant") double pregnant,
            @RequestParam("plasma") double plasma,
            @RequestParam("pressure") double pressure,
            @RequestParam("thickness") double thickness,
            @RequestParam("insulin") double insulin,
            @RequestParam("bmi") double bmi,
            @RequestParam("pedigree") double pedigree,
            @RequestParam("age") double age,
            Model model) {

        try {
            // Python script path
            String pythonScriptPath = "diabetes_deep.py";
            
            // Prepare command arguments
            List<String> command = new ArrayList<>();
            command.add("python");
            command.add(pythonScriptPath);
            command.add(String.valueOf(pregnant));
            command.add(String.valueOf(plasma));
            command.add(String.valueOf(pressure));
            command.add(String.valueOf(thickness));
            command.add(String.valueOf(insulin));
            command.add(String.valueOf(bmi));
            command.add(String.valueOf(pedigree));
            command.add(String.valueOf(age));

            ProcessBuilder pb = new ProcessBuilder(command);
            
            // Set working directory
            File pythonDir = getPythonDirectory();
            
            if (pythonDir.exists()) {
                pb.directory(pythonDir);
            } else {
                 // Debug info if not found
                 StringBuilder output = new StringBuilder();
                 output.append("Error: Could not find Python directory. \n");
                 output.append("User Dir: ").append(System.getProperty("user.dir")).append("\n");
                 model.addAttribute("result", output.toString());
                 return "deep/diabetes";
            }
            
            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8")); // Changed to UTF-8
            
            String line;
            StringBuilder output = new StringBuilder();
            
            while ((line = reader.readLine()) != null) {
                // Filter output to show only results, hiding system logs
                if (line.contains("예측 결과:") || line.contains("확률:")) {
                    output.append(line).append("\n");
                }
            }
            
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                output.append("Process exited with code ").append(exitCode);
            }

            model.addAttribute("result", output.toString());
            
            // Pass back input values so form remains filled
            model.addAttribute("pregnant", pregnant);
            model.addAttribute("plasma", plasma);
            model.addAttribute("pressure", pressure);
            model.addAttribute("thickness", thickness);
            model.addAttribute("insulin", insulin);
            model.addAttribute("bmi", bmi);
            model.addAttribute("pedigree", pedigree);
            model.addAttribute("age", age);

        } catch (Exception e) {
            model.addAttribute("result", "Error: " + e.getMessage());
        }

        return "deep/diabetes";
    }

    @GetMapping("/deep/lung")
    public String lungForm() {
        return "deep/lung";
    }

    @PostMapping("/deep/lung/predict")
    public String lungPredict(
            @RequestParam("attr1") double attr1,
            @RequestParam("attr2") double attr2,
            @RequestParam("attr3") double attr3,
            @RequestParam("attr4") double attr4,
            @RequestParam("attr5") double attr5,
            @RequestParam("attr6") double attr6,
            @RequestParam("attr7") double attr7,
            @RequestParam("attr8") double attr8,
            @RequestParam("attr9") double attr9,
            @RequestParam("attr10") double attr10,
            @RequestParam("attr11") double attr11,
            @RequestParam("attr12") double attr12,
            @RequestParam("attr13") double attr13,
            @RequestParam("attr14") double attr14,
            @RequestParam("attr15") double attr15,
            @RequestParam("attr16") double attr16,
            Model model) {

        try {
            // Python script path
            String pythonScriptPath = "lung_deep.py";
            
            // Prepare command arguments
            List<String> command = new ArrayList<>();
            command.add("python");
            command.add(pythonScriptPath);
            command.add(String.valueOf(attr1));
            command.add(String.valueOf(attr2));
            command.add(String.valueOf(attr3));
            command.add(String.valueOf(attr4));
            command.add(String.valueOf(attr5));
            command.add(String.valueOf(attr6));
            command.add(String.valueOf(attr7));
            command.add(String.valueOf(attr8));
            command.add(String.valueOf(attr9));
            command.add(String.valueOf(attr10));
            command.add(String.valueOf(attr11));
            command.add(String.valueOf(attr12));
            command.add(String.valueOf(attr13));
            command.add(String.valueOf(attr14));
            command.add(String.valueOf(attr15));
            command.add(String.valueOf(attr16));

            ProcessBuilder pb = new ProcessBuilder(command);
            
            // Set working directory
            File pythonDir = getPythonDirectory();
            
            if (pythonDir.exists()) {
                pb.directory(pythonDir);
            } else {
                 StringBuilder output = new StringBuilder();
                 output.append("Error: Could not find Python directory. \n");
                 model.addAttribute("result", output.toString());
                 return "deep/lung";
            }
            
            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
            
            String line;
            StringBuilder output = new StringBuilder();
            
            while ((line = reader.readLine()) != null) {
                if (line.contains("예측 결과:") || line.contains("확률:")) {
                    output.append(line).append("\n");
                }
            }
            
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                output.append("Process exited with code ").append(exitCode);
            }

            model.addAttribute("result", output.toString());
            
            // Pass back input values
            model.addAttribute("attr1", attr1);
            model.addAttribute("attr2", attr2);
            model.addAttribute("attr3", attr3);
            model.addAttribute("attr4", attr4);
            model.addAttribute("attr5", attr5);
            model.addAttribute("attr6", attr6);
            model.addAttribute("attr7", attr7);
            model.addAttribute("attr8", attr8);
            model.addAttribute("attr9", attr9);
            model.addAttribute("attr10", attr10);
            model.addAttribute("attr11", attr11);
            model.addAttribute("attr12", attr12);
            model.addAttribute("attr13", attr13);
            model.addAttribute("attr14", attr14);
            model.addAttribute("attr15", attr15);
            model.addAttribute("attr16", attr16);

        } catch (Exception e) {
            model.addAttribute("result", "Error: " + e.getMessage());
        }

        return "deep/lung";
    }
}
