package mvp.presenter;

import mvp.MVPView;

public abstract class AbstractPresenter<VIEW extends MVPView> implements Presenter<VIEW> {

  protected VIEW view;

  @Override
  public VIEW getView() {
    return view;
  }

  @Override
  public void takeView(VIEW view) {
    this.view = view;
    onTakeView();
  }

  @Override
  public void dropView() {
    onDropView();
    view = null;
  }

  public abstract void onDropView();

  public abstract void onTakeView();

  public abstract void destroy();
}