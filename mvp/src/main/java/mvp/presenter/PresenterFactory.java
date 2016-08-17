package mvp.presenter;

import mvp.MVPView;

public interface PresenterFactory {
  Presenter createPresenter(MVPView view);
}