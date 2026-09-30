// aarohi's code
import java.util.ArrayList;
import java.util.List;

public class paragraph {
private List<textrun> runs;
public paragraph(){
    runs=new ArrayList<>();
}
public void addRun(textrun run){
    runs.add(run);
}
public List<textrun> getRuns(){
    return new ArrayList<>(runs) ;
}
}
