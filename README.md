# resume-maker-App
The **Resume Maker App** is a powerful JavaFX desktop application designed to help users create professional, stylish, and customizable resumes quickly and easily. This project was developed as part of an internship at **Vault of Codes** and demonstrates skills in Java GUI development, file handling, JSON, PDF export, and UI/UX design.


## ✨ Features

- 🔹 Step-by-step Resume Builder (Fields → Styling → Template → PDF)
- 🔹 Dynamic input fields (Education, Skills, Projects, Internships, Experience)
- 🔹 Save/Load resume drafts using JSON (Gson)
- 🔹 Export to high-quality PDF (iTextPDF)
- 🔹 Upload and display profile photo
- 🔹 Real-time live preview
- 🔹 Choose from multiple templates (Modern, Classic, Dark)
- 🔹 Customize fonts, sizes, colors, margins, bold/italic text
- 🔹 Internationalization (i18n) with multi-language support
- 🔹 Dark mode and responsive layout
- 🔹 Resume scoring and AI suggestions (optional features)
- 🔹 Email sharing (planned)
- 🔹 Built in **Java 22 + JavaFX**


## 📂 Project Structure

Final Project/

├── .vscode/ # VSCode project settings

├── Lib/ # External libraries (Gson, iTextPDF)

├── Resources/ # Fonts, images, icons

│ └── fonts/ # Custom fonts (e.g., DejaVu)

├── javafx-sdk-24.0.1/ # JavaFX SDK

├── ResumeMakerApp.java # Main JavaFX class

├── ResumeData.java # Data model for resume content

├── *.class # Compiled files

└── Resume Maker.exe # Exported .exe


Download JavaFX SDK 24.0.1

https://gluonhq.com/products/javafx/

Extract and place in a known location 


Run the app

Press F5 in VS Code

Or use java ResumeMakerApp with appropriate classpath


🖋️ Fonts
If using DejaVu Sans for PDF export:

Add DejaVuSans.ttf to Resources/fonts/

Load in Java:  BaseFont bf = BaseFont.createFont("Resources/fonts/DejaVuSans.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);


👨‍💻 Developer Info

Developed by Shalu Baloda

Java Intern @ Vault of Codes


## 📝 License

This project is licensed under the MIT LICENCE (https://github.com/shalu142/Resume-Maker-App/blob/main/LICENCE).


