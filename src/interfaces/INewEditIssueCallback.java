package interfaces;

import model.Issue;

public interface INewEditIssueCallback {
    boolean saveNew(Issue issue);
    boolean saveChanges(Issue issue, int index);
}
