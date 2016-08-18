# MVP
A small MVP framework.  Wire in your Views and Presenters by creating a PresenterService for your views to bind to. Presenters are provided views when bound. The PresenterService will handle unbinding views and rebinding on configuration changes.  The presenters are held in memory until your activity is destroyed to provide a more seamless experince and means you have to manage less state!  The framework uses annotation processing to minimise boilerplate.  You have to annotate your presenters and implement lifecycle callbacks.  Since you'll bind and implement the callbacks in your abstract activity or fragment you should only need to write one word (@Presenter) to wire in new views & presenters. :)

# Gradle
In your build script add
```
    repositories {
        maven { url 'https://dl.bintray.com/serenskye/mvp' }
    }
```

Then in dependancies add
```
compile 'com.mvp:mvp:X.X.X'
apt 'com.mvp:mvp-compiler:X.X.X'
```

# Pre-setup
It's recommended that you provide access to the PresenterService via a singleton, you can use the Application or ServiceLocator pattern. 

# How to use
Note you will have to run build -> rebuild project in android studio to generate the files

1. Extend the interface MVPView

2. Fragments and Activities implement a child interface of MVPView

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
  