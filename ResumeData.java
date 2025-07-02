import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResumeData {
    // Basic fields
    public String fullName;
    public String email;
    public String phone;

    // Profile photo path
    public String profilePhotoPath;

    // Dynamic sections
    public List<String[]> education = new ArrayList<>(); // course, stream, institute, grade
    public List<String> skills = new ArrayList<>();
    public List<String> projects = new ArrayList<>();
    public List<String> internships = new ArrayList<>();
    public List<String> experience = new ArrayList<>();

    // Custom fields
    public Map<String, String> customFields = new HashMap<>();

    // Styling options
    public String fontColor;
    public String fontStyle;
    public int fontSize;
    public int margin;
    public boolean bold;
    public boolean italic;

    // Selected template
    public String selectedTemplate;

    // Social links
    public String linkedin;
    public String github;

    // Achievements and credentials
    public List<String> awards = new ArrayList<>();
    public List<String> certificates = new ArrayList<>();
    public List<String> licenses = new ArrayList<>();

    // Optional: Generic extra fields (if needed)
    public Map<String, String> fields = new HashMap<>();
}
