package mvp.presenter;

import android.os.Bundle;
import android.support.annotation.Nullable;

import mvp.MVPView;

public interface PresenterServiceInternal {
  void savePresenter(String id, Bundle bundle);

  void dropView(String id, boolean retain);

  void takeView(MVPView view, Bundle bundle);

  void takeChild(MVPView view);

  @Nullable
  Presenter getCurrentParent();

  void onPresenterViewPaused(String id);

  void onPresenterViewResumed(String id);

  void onPresenterViewReady(String id);
}
