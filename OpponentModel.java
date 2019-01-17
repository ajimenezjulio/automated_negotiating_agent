import negotiator.issue.Issue;
import negotiator.issue.IssueDiscrete;
import negotiator.issue.ValueDiscrete;

import java.util.HashMap;
import java.util.Map;

public class OpponentModel {
    public HashMap<String, IssueStore> issues;
    private String opponentName;

    public OpponentModel() {
        this.issues = new HashMap<>();
    }

    public String getName() {
        return opponentName;
    }

    public void setName(String opponentName) {
        this.opponentName = opponentName;
    }

    public void addIssue(IssueDiscrete issue) {
        if(!this.issues.containsKey(issue.getName())) {
            this.issues.put(issue.getName(), new IssueStore(issue));
        }
    }

    public IssueStore getIssue(IssueDiscrete issue) {
        return this.issues.getOrDefault(issue.getName(), null);
    }

    public class IssueStore {
        public IssueDiscrete issue;
        public HashMap<String, Integer> values = new HashMap<>();

        public IssueStore(IssueDiscrete issue) {
            this.issue = issue;
        }

        public void addValue(ValueDiscrete valueDiscrete) {
            Integer valueCount = this.values.getOrDefault(valueDiscrete.getValue(), 0);
            this.values.put(valueDiscrete.getValue(), valueCount);
        }

        public void addCount(ValueDiscrete valueDiscrete) {
            if(this.values.containsKey(valueDiscrete.getValue())) {
                Integer valueCount = this.values.get(valueDiscrete.getValue());
                valueCount++;
                this.values.put(valueDiscrete.getValue(), valueCount);
            }
        }

        public Integer getValueCount(ValueDiscrete valueDiscrete) {
            return this.values.getOrDefault(valueDiscrete, null);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, IssueStore> entry: this.issues.entrySet()) {
            sb.append("\t" + entry.getKey() + ": " + entry.getValue().values + "\n");
        }
        return sb.toString();
    }

    public static void main(String[] vars) {
        OpponentModel model = new OpponentModel();
        IssueDiscrete idd = new IssueDiscrete("A", 1, new String[] {"abc"});
        model.addIssue(idd);

        IssueStore id = model.getIssue(idd);

        ValueDiscrete vd = id.issue.getValue(0);
        id.addCount(vd);
        id.addCount(vd);

        System.out.println(model.getIssue(idd).getValueCount(vd));
    }
}
