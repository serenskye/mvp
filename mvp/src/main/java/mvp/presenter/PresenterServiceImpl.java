package mvp.presenter;

import android.os.Bundle;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import mvp.MVPView;

/**
 * When you create a presenter you must call takeView or takeChild (depending on if your a root view (activity) or a child view (fragment).
 * Your MVPView will then be injected with a {@link PresenterLifecycleListener} instance, which you must wire into the lifecycle callbacks. The
 * Basefragment or BaseActivity should manage this.  Thats it! Your presenter is ready to go and auto managed.
 *
 * Works whether you have set a parentPresenter or not, this is good when you want to have dumb activities.
 */
public class PresenterServiceImpl implements PresenterService, PresenterServiceInternal {
  static final String PRESENTER_ID_KEY = "com.joincoup.app.presentation.PRESENTER_ID_KEY;";
  static final String NO_PRESENTER_ID = "";

  Map<String, PresenterNode> presenters = new HashMap<>();
  String currentParentId;
  private PresenterFactory presenterFactory;

  public PresenterServiceImpl(PresenterFactory presenterFactory) {
    this.presenterFactory = presenterFactory;
  }

  /**
   * use this to reset parent
   */
  public void clearParent() {
    currentParentId = NO_PRESENTER_ID;
  }

  /**
   * Call this when you initialise your activity
   */
  public void takeView(MVPView view, Bundle bundle) {
    PresenterNode presenterNode;
    if (!presenterAlreadyExists(bundle)) {
      Presenter presenter = presenterFactory.createPresenter(view);
      presenterNode = new PresenterNode(presenter);
      presenters.put(presenterNode.id, presenterNode);
    } else {
      presenterNode = getPresenterFromBundle(bundle);
    }
    PresenterLifecycleListenerImpl presenterLifecycleListener = new PresenterLifecycleListenerImpl(this, presenterNode.id);
    presenterNode.node.takeView(view);
    view.setLifecycleListener(presenterLifecycleListener);
    currentParentId = presenterNode.id;
  }

  /**
   * Called by fragments
   */
  public void takeChild(MVPView view) {
    Presenter presenter = presenterFactory.createPresenter(view);
    PresenterNode child = new PresenterNode(presenter, currentParentId);
    presenters.put(child.id, child);
    PresenterLifecycleListenerImpl presenterLifecycleListener = new PresenterLifecycleListenerImpl(this, child.id);
    presenter.takeView(view);
    view.setLifecycleListener(presenterLifecycleListener);
  }

  public void onPresenterViewResumed(String id) {
    if (presenters.containsKey(id)) {
      PresenterNode node = presenters.get(id);
      if (node.isParent) {
        currentParentId = id;
      }
      node.getPresenter().onResume();
    }
  }

  public void onPresenterViewPaused(String id) {
    if (presenters.containsKey(id)) {
      PresenterNode node = presenters.get(id);
      node.getPresenter().onPause();
    }
  }

  public void onPresenterViewReady(String id) {
    if (presenters.containsKey(id)) {
      PresenterNode node = presenters.get(id);
      node.getPresenter().onViewReady();
    }
  }

  @Nullable
  @Override
  public Presenter getCurrentParent() {
    return getPresenter(currentParentId);
  }

  /**
   * @param id - id of presenter to drop
   * @param retain - keep the presenter around in case we reattach - for activities
   */
  public void dropView(String id, boolean retain) {
    if (!presenters.containsKey(id)) {
      return;
    }

    PresenterNode presenterNode = presenters.get(id);

    if (!retain) {
      //KILL
      List<String> toDestroy = new ArrayList<>();
      toDestroy.add(presenterNode.id);
      if (presenterNode.isParent) {
        //find children to kill
        for (PresenterNode node : presenters.values()) {
          if (!node.isParent && node.parentId.equals(id)) {
            toDestroy.add(node.id);
          }
        }
      }
      destroyPresenters(toDestroy);
    } else {
      dropView(presenterNode.getPresenter());
    }
  }

  private void dropView(Presenter presenter) {
    if (presenter.getView() != null) {
      presenter.getView().setLifecycleListener(null);
      presenter.dropView();
    }
  }

  private void destroyPresenters(List<String> ids) {
    for (String id : ids) {
      Presenter presenter = getPresenter(id);
      dropView(presenter);
      presenter.destroy();
      presenters.remove(id);

      if (id.equals(currentParentId)) {
        currentParentId = NO_PRESENTER_ID;
      }
    }
  }

  public Presenter getPresenter(String id) {
    if (presenters.containsKey(id)) {
      return presenters.get(id).node;
    }
    throw new RuntimeException("presenter does not exist!" + id);
  }

  public void savePresenter(String id, Bundle bundle) {
    if (presenters.containsKey(id) && presenters.get(id).isParent) {
      bundle.putString(PRESENTER_ID_KEY, id);
    }
  }

  private PresenterNode getPresenterFromBundle(Bundle bundle) {
    if (bundle != null && bundle.containsKey(PRESENTER_ID_KEY)) {
      //presenter already exists!
      String id = bundle.getString(PRESENTER_ID_KEY);
      if (presenters.containsKey(id)) {
        return presenters.get(id);
      }
    }
    throw new RuntimeException("app does not have the requested presenter");
  }

  private boolean presenterAlreadyExists(Bundle bundle) {
    if (bundle != null && bundle.containsKey(PRESENTER_ID_KEY)) {
      //presenter already exists!
      String id = bundle.getString(PRESENTER_ID_KEY);
      if (presenters.containsKey(id)) {
        return true;
      }
      //maybe the app has been killed and so we don't have our presenters anymore!
    }
    return false;
  }

  static final class PresenterNode {
    private boolean isParent;
    private Presenter node;
    private String id;
    private String parentId;

    public PresenterNode(Presenter node) {
      this.node = node;
      this.isParent = true;
      id = UUID.randomUUID().toString();
    }

    public PresenterNode(Presenter node, String parentId) {
      this.node = node;
      this.isParent = false;
      this.parentId = parentId;
      id = UUID.randomUUID().toString();
    }

    public String getId() {
      return id;
    }

    public Presenter getPresenter() {
      return node;
    }
  }
}
