package com.python.deep.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Controller
public class DeepLearningController {

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
            String userDir = System.getProperty("user.dir");
            File pythonDir = new File(userDir, "src/main/resources/static/python");
            
            if (!pythonDir.exists()) {
                pythonDir = new File(userDir, "springboot/src/main/resources/static/python");
            }
            
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
            // Use relative path and English filename to avoid encoding issues
            // We will set the working directory to the python folder, so we just need the filename
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
            
            // Set working directory to where the script is, to avoid path encoding issues
            // Try to find the directory relative to user.dir
            String userDir = System.getProperty("user.dir");
            File pythonDir = new File(userDir, "src/main/resources/static/python"); // Standard structure
            
            if (!pythonDir.exists()) {
                // Try inside springboot folder (if run from parent)
                pythonDir = new File(userDir, "springboot/src/main/resources/static/python");
            }
            
            StringBuilder output = new StringBuilder();

            if (pythonDir.exists()) {
                pb.directory(pythonDir);
            } else {
                 // Debug info if not found
                 output.append("Error: Could not find Python directory. \n");
                 output.append("User Dir: ").append(userDir).append("\n");
                 output.append("Tried: ").append(new File(userDir, "src/main/resources/static/python").getAbsolutePath()).append("\n");
                 output.append("Tried: ").append(new File(userDir, "springboot/src/main/resources/static/python").getAbsolutePath()).append("\n");
            }
            
            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8")); // Changed to UTF-8
            
            String line;
            
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
}
