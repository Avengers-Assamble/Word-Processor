
// aarohi's code
import java.util.ArrayList;
import java.util.List;

public class document {
private List<paragraph> Paragraphs;
public document(){
    this.Paragraphs=new ArrayList<>();
}
public void insertParagraphs(paragraph para){
    Paragraphs.add(para);
}
public List<paragraph> getParagraphs(){
    return new ArrayList<>(Paragraphs);
}
    public void insertParagraphAt(int index, paragraph para) {
        Paragraphs.add(index, para);
    }
    public void setParagraph(int index, paragraph para) {
        Paragraphs.set(index, para);
    }
    public void removeParagraph(int index) {
        Paragraphs.remove(index);
    }
    public void setParagraphs(List<paragraph> list) {
        Paragraphs = new ArrayList<>(list);
    }

}
