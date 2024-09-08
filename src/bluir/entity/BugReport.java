package bluir.entity;

import java.util.Set;

public class BugReport {
	private String bugId;
	private String summary;
	private String description;
	private Set<String> fixedFiles;

	public BugReport() {
	}

	public BugReport(String bugId, String summary, String description, Set<String> fixedFiles) {
		this.bugId = bugId;
		this.summary = summary;
		this.description = description;
		this.fixedFiles = fixedFiles;
	}

	public String getBugId() {
		return bugId;
	}

	public void setBugId(String bugId) {
		this.bugId = bugId;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Set<String> getFixedFiles() {
		return fixedFiles;
	}

	public void setFixedFiles(Set<String> fixedFiles) {
		this.fixedFiles = fixedFiles;
	}

	@Override
	public String toString() {
		return "BugReport [bugId=" + bugId + ", summary=" + summary + ", description=" + description + ", fixedFiles=" + fixedFiles + "]";
	}
}
