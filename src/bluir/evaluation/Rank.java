package bluir.evaluation;

public class Rank
{
  public int bugID;
  public int fileID;
  public int rank;
  public double score;
	@Override
	public String toString() {
		return "Rank [bugID=" + bugID + ", fileID=" + fileID + ", rank=" + rank + ", score=" + score + "]";
	}
}