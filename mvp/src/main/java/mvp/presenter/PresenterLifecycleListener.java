package mvp.presenter;

import android.os.Bundle;

public interface PresenterLifecycleListener {
  void onPause();
  void onResume();
  void onViewReady();
  void onDestroy();
  void onDestroy(boolean retain);
  void onSaveInstanceState(Bundle bundle);
}
