package mvp.presenter;

import android.os.Bundle;

public class PresenterLifecycleListenerImpl implements PresenterLifecycleListener {

  private final PresenterServiceInternal presenterService;
  private final String id;

  public PresenterLifecycleListenerImpl(PresenterServiceInternal presenterService, String id) {
    this.presenterService = presenterService;
    this.id = id;
  }

  public void onPause() {
    presenterService.onPresenterViewPaused(id);
  }

  public void onResume() {
    presenterService.onPresenterViewResumed(id);
  }

  @Override
  public void onViewReady() {
      presenterService.onPresenterViewReady(id);
  }

  public void onDestroy() {
    //fragments are not retained as they retain themselves during lifecycle changes
    presenterService.dropView(id, false);
  }

  @Override
  public void onDestroy(boolean retain) {
    presenterService.dropView(id, retain);
  }

  //should only be called by root views
  public void onSaveInstanceState(Bundle bundle) {
    presenterService.savePresenter(id, bundle);
  }
}
