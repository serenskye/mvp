package mvp.presenter;

import mvp.MVPView;

public interface Presenter<T extends MVPView> {

  void onResume();
  void onPause();

  T getView();

  void takeView(final T view);
  void dropView();

  void destroy();
}

