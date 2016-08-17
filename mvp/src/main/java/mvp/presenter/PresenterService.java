package mvp.presenter;

import android.os.Bundle;

import mvp.MVPView;

//consumed by views
public interface PresenterService {

  void takeView(MVPView view, Bundle bundle);

  void takeChild(MVPView view);
}
