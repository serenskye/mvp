package mvp.presenter;

import android.os.Bundle;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Bundle.class)
public class PresenterLifecycleListenerImplTest {

  PresenterServiceImpl presenterService;
  PresenterLifecycleListenerImpl lifecycleListener;
  Presenter presenter;
  String testPresenterId;
  Bundle bundle;

  @Before
  public void setup() {
    presenterService = spy(new PresenterServiceImpl(mock(PresenterFactory.class)));
    presenter = mock(Presenter.class);

    PresenterServiceImpl.PresenterNode node = new PresenterServiceImpl.PresenterNode(presenter);
    presenterService.presenters.put(node.getId(), node);
    testPresenterId = presenterService.currentParentId;

    lifecycleListener = new PresenterLifecycleListenerImpl(presenterService, testPresenterId);

    bundle = PowerMockito.mock(Bundle.class);
  }

  @Test
  public void testOnResume_presenterOnResume() {
    lifecycleListener.onResume();
    verify(presenterService).onPresenterResumed(testPresenterId);
  }

  @Test
  public void testOnPause_presenterOnPause() {
    lifecycleListener.onPause();
    verify(presenterService).onPresenterPaused(testPresenterId);
  }

  @Test
  public void testOnDestroy_presenterServiceDropsView() {
    lifecycleListener.onDestroy();
    verify(presenterService).dropView(testPresenterId, false);
  }

  @Test
  public void testOnDestroy_Retain_presenterServiceDropsView() {
    lifecycleListener.onDestroy(true);
    verify(presenterService).dropView(testPresenterId, true);
  }

  @Test
  public void testOnSaveInstanceState_presenterSaves() {
    lifecycleListener.onSaveInstanceState(bundle);
    verify(presenterService).savePresenter(testPresenterId, bundle);
  }
}

