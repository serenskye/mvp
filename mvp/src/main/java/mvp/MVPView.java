package mvp;

import mvp.presenter.PresenterLifecycleListener;
import rx.Observable;

public interface MVPView {
  <T> Observable.Transformer<T, T> getLifecycleBinder();

  void showNoNetworkConnection();

  void hideNoNetworkConnection();

  void setLifecycleListener(PresenterLifecycleListener presenterLifecycleListener);


}
