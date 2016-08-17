# mvp
A small MVP framework.  

# pre-setup
It's recommended that you provide access to the PresenterService via a singleton, you can use the Application or ServiceLocator pattern. 

# how to use
1. Extend the interface MVPView

2. Fragments and Activities implement a child of MPPView

3. Bind your Activity or Fragment to the presenter
```@Override
  protected void onPostCreate(@Nullable Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    ViewServiceLocator.getPresenterService().takeView(this, savedInstanceState);
  }
```

4. Implement the interface for MVPView - you must implement setLifecycleListener, then forward lifecycle callbacks to it as follows.  I recommend putting this in an Abstract Activity class.

```@Override
  public void onResume() {
    super.onResume();
    if (presenterLifecycleListener != null) {
      presenterLifecycleListener.onResume();
    }
  }

  @Override
  public void onPause() {
    super.onPause();
    if (presenterLifecycleListener != null) {
      presenterLifecycleListener.onPause();
    }
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    if (presenterLifecycleListener != null) {
      presenterLifecycleListener.onSaveInstanceState(outState);
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();

    if (unbinder != null) {
      unbinder.unbind();
    }

    if (presenterLifecycleListener != null) {
      presenterLifecycleListener.onDestroy(!isFinishing() || isChangingConfigurations());
    }
  }

  @Override
  public void setLifecycleListener(PresenterLifecycleListener presenterLifecycleListener) {
    this.presenterLifecycleListener = presenterLifecycleListener;
  } 
```

  5. Create a presenter which extends AbstractPresenter of type view.  Make sure you annotate it with Presenter.  

```@Presenter
public class HomeViewPresenter extends AbstractPresenter<HomeView> {.....
```

6. Thats it!
  