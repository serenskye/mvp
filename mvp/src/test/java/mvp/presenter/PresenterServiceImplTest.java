package mvp.presenter;

import android.os.Bundle;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import mvp.MVPView;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Bundle.class)
public class PresenterServiceImplTest {

  PresenterServiceImpl presenterService;
  PresenterFactory presenterFactory;
  Bundle bundle;

  @Before
  public void setup() {
    presenterFactory = mock(PresenterFactory.class);
    presenterService = new PresenterServiceImpl(presenterFactory);
    bundle = PowerMockito.mock(Bundle.class);
  }

  @Test
  public void testTakeView_ParentDoesNotExist_CreatesNewPresenter() {
    MVPView view = mock(MVPView.class);
    Presenter presenter = mock(Presenter.class);
    when(presenterFactory.createPresenter(view)).thenReturn(presenter);

    presenterService.takeView(view, null);
    Assert.assertEquals(presenterService.getCurrentParent(), presenter);
  }

  @Test
  public void testTakeView_ParentDoesNotExist_VerifyLifecycleListenerSet() {
    MVPView view = mock(MVPView.class);
    Presenter presenter = mock(Presenter.class);
    when(presenterFactory.createPresenter(view)).thenReturn(presenter);

    presenterService.takeView(view, null);
    verify(view).setLifecycleListener(any(PresenterLifecycleListener.class));
  }

  @Test
  public void testTakeView_ParentDoesNotExist_VerifyPresenterTakeView() {
    MVPView view = mock(MVPView.class);
    Presenter presenter = mock(Presenter.class);
    when(presenterFactory.createPresenter(view)).thenReturn(presenter);

    presenterService.takeView(view, null);
    verify(presenter).takeView(view);
  }

  @Test
  public void testTakeView_rootPresenterExistsForId_VerifyPresenterIsNotCreated() {
    MVPView view = mock(MVPView.class);
    Presenter presenter = mock(Presenter.class);
    Presenter presenter2 = mock(Presenter.class);
    when(presenterFactory.createPresenter(view)).thenReturn(presenter).thenReturn(presenter2);

    //create the presenter
    presenterService.takeView(view, null);

    String id = presenterService.currentParentId;
    when(bundle.containsKey(PresenterServiceImpl.PRESENTER_ID_KEY)).thenReturn(true);
    when(bundle.getString(anyString())).thenReturn(id);

    presenterService.savePresenter(id, bundle);

    //don't create the presenter again, should be the same presenter
    presenterService.takeView(view, bundle);

    Assert.assertEquals(presenterService.getCurrentParent(), presenter);
    verify(presenterFactory.createPresenter(view), never());
  }

  @Test
  public void testTakeView_ParentExistsForId_VerifyPresenterTakeView() {
    MVPView view = mock(MVPView.class);
    Presenter presenter = mock(Presenter.class);
    when(presenterFactory.createPresenter(view)).thenReturn(presenter);

    //create the presenter
    presenterService.takeView(view, null);

    String id = presenterService.currentParentId;
    when(bundle.getString(PresenterServiceImpl.PRESENTER_ID_KEY)).thenReturn(id);
    when(bundle.containsKey(PresenterServiceImpl.PRESENTER_ID_KEY)).thenReturn(true);
    presenterService.savePresenter(id, bundle);

    //don't create the presenter again, should be the same presenter
    presenterService.takeView(view, bundle);

    verify(presenter, times(2)).takeView(view);
  }

  @Test
  public void testTakeView_ParentExistForId_VerifyLifecycleListenerSet() {
    MVPView view = mock(MVPView.class);
    Presenter presenter = mock(Presenter.class);
    when(presenterFactory.createPresenter(view)).thenReturn(presenter);

    //create the presenter
    presenterService.takeView(view, null);

    String id = presenterService.currentParentId;
    when(bundle.getString(PresenterServiceImpl.PRESENTER_ID_KEY)).thenReturn(id);
    when(bundle.containsKey(PresenterServiceImpl.PRESENTER_ID_KEY)).thenReturn(true);
    presenterService.savePresenter(id, bundle);

    //don't create the presenter again, should be the same presenter
    presenterService.takeView(view, bundle);

    verify(view, times(2)).setLifecycleListener(any(PresenterLifecycleListener.class));
  }

  @Test
  public void testTakeView_ParentExistsButDoesNotMatchId_CreateNewPresenter() {
    MVPView view = mock(MVPView.class);
    Presenter presenter = mock(Presenter.class);
    Presenter presenter2 = mock(Presenter.class);
    when(presenterFactory.createPresenter(view)).thenReturn(presenter).thenReturn(presenter2);

    //create the presenter
    presenterService.takeView(view, null);
    when(bundle.getString(PresenterServiceImpl.PRESENTER_ID_KEY)).thenReturn("invalid ID");
    when(bundle.containsKey(PresenterServiceImpl.PRESENTER_ID_KEY)).thenReturn(true);

    presenterService.savePresenter("invalid ID", bundle);

    //don't create the presenter again, should be the same presenter
    presenterService.takeView(view, bundle);

    Assert.assertEquals(presenterService.getCurrentParent(), presenter2);
  }

  @Test
  public void testTakeChild_CurrentPresenterRemainsTheSame() {
    MVPView view = mock(MVPView.class);
    Presenter presenter = mock(Presenter.class);
    Presenter presenter2 = mock(Presenter.class);
    Presenter presenter3 = mock(Presenter.class);
    when(presenterFactory.createPresenter(view)).thenReturn(presenter).thenReturn(presenter2).thenReturn(presenter3);

    //create the presenter
    presenterService.takeView(view, null);

    //don't create the presenter again, should be the same presenter
    presenterService.takeChild(view);
    presenterService.takeChild(view);

    Assert.assertEquals(presenterService.getCurrentParent(), presenter);
  }

  @Test
  public void testTakeChild_childrenAreAdded() {
    MVPView view = mock(MVPView.class);
    Presenter child1 = mock(Presenter.class);
    Presenter child2 = mock(Presenter.class);
    when(presenterFactory.createPresenter(view)).thenReturn(child1).thenReturn(child2);

    //don't create the presenter again, should be the same presenter
    presenterService.takeChild(view);
    presenterService.takeChild(view);

    Assert.assertEquals(presenterService.presenters.size(), 2);
  }

  @Test
  public void testTakeChild_VerifyPresenterTakeView() {
    MVPView view = mock(MVPView.class);
    Presenter presenter = mock(Presenter.class);
    when(presenterFactory.createPresenter(view)).thenReturn(presenter);

    //create the presenter
    presenterService.takeChild(view);
    verify(presenter).takeView(view);
  }

  @Test
  public void testTakeChild_VerifyLifecycleListenerSet() {
    MVPView view = mock(MVPView.class);
    Presenter presenter = mock(Presenter.class);
    when(presenterFactory.createPresenter(view)).thenReturn(presenter);

    //create the presenter
    presenterService.takeChild(view);

    verify(view).setLifecycleListener(any(PresenterLifecycleListener.class));
  }

  @Test
  public void testDropChild_verifyPresenterDropView() {
    MVPView view = mock(MVPView.class);
    Presenter presenter = mock(Presenter.class);
    when(presenter.getView()).thenReturn(view);
    PresenterServiceImpl.PresenterNode presenterNode = new PresenterServiceImpl.PresenterNode(presenter);
    presenterService.presenters.put(presenterNode.getId(), presenterNode);
    presenter.takeView(view);

    presenterService.dropView(presenterNode.getId(), true);

    verify(presenter).dropView();
  }

  @Test
  public void testDropChild_ViewIsNull_verifyPresenterDoesNotDropView() {
    MVPView view = mock(MVPView.class);
    Presenter presenter = mock(Presenter.class);
    PresenterServiceImpl.PresenterNode presenterNode = new PresenterServiceImpl.PresenterNode(presenter);
    presenterService.presenters.put(presenterNode.getId(), presenterNode);
    presenter.takeView(view);

    presenterService.dropView(presenterNode.getId(), true);

    verify(presenter, never()).dropView();
  }

  @Test
  public void testDropChild_verifyPresenterLifeCycleListenerIsSetToNull() {
    MVPView view = mock(MVPView.class);
    Presenter presenter = mock(Presenter.class);
    when(presenter.getView()).thenReturn(view);
    PresenterServiceImpl.PresenterNode presenterNode = new PresenterServiceImpl.PresenterNode(presenter);
    presenterService.presenters.put(presenterNode.getId(), presenterNode);
    presenter.takeView(view);

    presenterService.dropView(presenterNode.getId(), true);

    verify(view).setLifecycleListener(null);
  }

  @Test
  public void testDropRoot_verifyPresenterDropView() {
    MVPView view = mock(MVPView.class);
    Presenter presenter = mock(Presenter.class);
    when(presenter.getView()).thenReturn(view);
    PresenterServiceImpl.PresenterNode presenterNode = new PresenterServiceImpl.PresenterNode(presenter);
    presenterService.presenters.put(presenterNode.getId(), presenterNode);
    presenterService.currentParentId = presenterNode.getId();
    presenter.takeView(view);

    presenterService.dropView(presenterNode.getId(), true);

    verify(presenter).dropView();
  }

  @Test
  public void testDropRoot_verifyPresenterLifeCycleListenerIsSetToNull() {
    MVPView view = mock(MVPView.class);
    Presenter presenter = mock(Presenter.class);
    when(presenter.getView()).thenReturn(view);
    PresenterServiceImpl.PresenterNode presenterNode = new PresenterServiceImpl.PresenterNode(presenter);
    presenterService.presenters.put(presenterNode.getId(), presenterNode);
    presenterService.currentParentId = presenterNode.getId();
    presenter.takeView(view);

    presenterService.dropView(presenterNode.getId(), true);

    verify(view).setLifecycleListener(null);
  }

  @Test
  public void testDropView_onUnknownId_dropViewNeverCalled() {
    MVPView view = mock(MVPView.class);
    Presenter presenter = mock(Presenter.class);
    PresenterServiceImpl.PresenterNode presenterNode = new PresenterServiceImpl.PresenterNode(presenter);
    presenterService.presenters.put(presenterNode.getId(), presenterNode);
    presenterService.currentParentId = presenterNode.getId();
    presenter.takeView(view);

    presenterService.dropView("UKNOWN", true);

    verify(presenter, never()).dropView();
  }

  @Test(expected = RuntimeException.class)
  public void testGetPresenter_unknownId_exception() {
    presenterService.getPresenter("UNKNOWN");
  }

  @Test
  public void testGetPresenter_isReturned() {
    Presenter presenter1 = mock(Presenter.class);
    PresenterServiceImpl.PresenterNode presenterNode1 = new PresenterServiceImpl.PresenterNode(presenter1);
    presenterService.presenters.put(presenterNode1.getId(), presenterNode1);

    Presenter presenter2 = mock(Presenter.class);
    PresenterServiceImpl.PresenterNode presenterNode2 = new PresenterServiceImpl.PresenterNode(presenter2);
    presenterService.presenters.put(presenterNode2.getId(), presenterNode2);

    Presenter presenter3 = mock(Presenter.class);
    PresenterServiceImpl.PresenterNode presenterNode3 = new PresenterServiceImpl.PresenterNode(presenter3);
    presenterService.presenters.put(presenterNode3.getId(), presenterNode3);

    presenterService.getPresenter(presenterNode1.getId());
    presenterService.getPresenter(presenterNode3.getId());
    presenterService.getPresenter(presenterNode2.getId());
  }

  @Test
  public void testSavePresenter_savesId() {
    Presenter presenter = mock(Presenter.class);
    PresenterServiceImpl.PresenterNode presenterNode = new PresenterServiceImpl.PresenterNode(presenter);
    presenterService.presenters.put(presenterNode.getId(), presenterNode);
    presenterService.currentParentId = presenterNode.getId();

    presenterService.savePresenter(presenterNode.getId(), bundle);
    verify(bundle).putString(PresenterServiceImpl.PRESENTER_ID_KEY, presenterNode.getId());
  }

  @Test
  public void testdropView_doNotRetain_removesPresenter() {
    MVPView view = mock(MVPView.class);
    Presenter presenter = mock(Presenter.class);
    when(presenterFactory.createPresenter(view)).thenReturn(presenter);

    presenterService.takeView(view, null);

    String currentParentId = presenterService.currentParentId;
    presenterService.dropView(presenterService.currentParentId, false);

    Assert.assertFalse(presenterService.presenters.containsKey(currentParentId));
  }

  @Test
  public void testdropCurrentView_doNotRetain_currentPresenterIsReset() {
    MVPView view = mock(MVPView.class);
    Presenter presenter = mock(Presenter.class);
    when(presenterFactory.createPresenter(view)).thenReturn(presenter);

    presenterService.takeView(view, null);

    presenterService.dropView(presenterService.currentParentId, false);

    Assert.assertEquals(presenterService.currentParentId, PresenterServiceImpl.NO_PRESENTER_ID);
  }

  @Test
  public void testdropView_doNotRetain_PresenterIsDestroyed() {
    MVPView view = mock(MVPView.class);
    Presenter presenter = mock(Presenter.class);
    when(presenterFactory.createPresenter(view)).thenReturn(presenter);

    presenterService.takeView(view, null);

    presenterService.dropView(presenterService.currentParentId, false);

    verify(presenter).destroy();
  }

  @Test
  public void testdropView_doNotRetain_ViewIsDropped() {
    MVPView view = mock(MVPView.class);
    Presenter presenter = mock(Presenter.class);
    when(presenter.getView()).thenReturn(view);
    when(presenterFactory.createPresenter(view)).thenReturn(presenter);

    presenterService.takeView(view, null);

    presenterService.dropView(presenterService.currentParentId, false);

    verify(presenter).dropView();
  }

  @Test
  public void testdropView_doNotRetain_ChildrenAreDestroyed() {
    MVPView view = mock(MVPView.class);
    Presenter presenter = mock(Presenter.class);

    Presenter presenter2 = mock(Presenter.class);
    Presenter presenter3 = mock(Presenter.class);

    when(presenterFactory.createPresenter(view)).thenReturn(presenter).thenReturn(presenter2).thenReturn(presenter3);

    presenterService.takeView(view, null);
    presenterService.takeChild(view);
    presenterService.takeChild(view);

    presenterService.dropView(presenterService.currentParentId, false);

    verify(presenter2).destroy();
    verify(presenter3).destroy();
  }

  @Test
  public void testdropView_doNotRetain_ChildrenAreDropped() {
    MVPView view = mock(MVPView.class);
    Presenter presenter = mock(Presenter.class);

    Presenter presenter2 = mock(Presenter.class);
    when(presenter2.getView()).thenReturn(view);
    Presenter presenter3 = mock(Presenter.class);
    when(presenter3.getView()).thenReturn(view);

    when(presenterFactory.createPresenter(view)).thenReturn(presenter).thenReturn(presenter2).thenReturn(presenter3);

    presenterService.takeView(view, null);
    presenterService.takeChild(view);
    presenterService.takeChild(view);

    presenterService.dropView(presenterService.currentParentId, false);

    verify(presenter2).dropView();
    verify(presenter3).dropView();
  }

  @Test
  public void testdropView_doNotRetain_ChildrenLifecycleIsNulled() {
    MVPView view = mock(MVPView.class);
    MVPView view2 = mock(MVPView.class);
    MVPView view3 = mock(MVPView.class);
    Presenter presenter = mock(Presenter.class);

    Presenter presenter2 = mock(Presenter.class);
    when(presenter2.getView()).thenReturn(view2);
    Presenter presenter3 = mock(Presenter.class);
    when(presenter3.getView()).thenReturn(view3);

    when(presenterFactory.createPresenter(any(MVPView.class))).thenReturn(presenter).thenReturn(presenter2).thenReturn(presenter3);

    presenterService.takeView(view, null);
    presenterService.takeChild(view2);
    presenterService.takeChild(view3);

    presenterService.dropView(presenterService.currentParentId, false);

    verify(view2).setLifecycleListener(null);
    verify(view3).setLifecycleListener(null);
  }

  @Test
  public void testdropView_doNotRetain_ChildrenAreRemoved() {
    MVPView view = mock(MVPView.class);
    Presenter presenter = mock(Presenter.class);

    Presenter presenter2 = mock(Presenter.class);
    when(presenter2.getView()).thenReturn(view);
    Presenter presenter3 = mock(Presenter.class);
    when(presenter3.getView()).thenReturn(view);

    when(presenterFactory.createPresenter(view)).thenReturn(presenter).thenReturn(presenter2).thenReturn(presenter3);

    presenterService.takeView(view, null);
    presenterService.takeChild(view);
    presenterService.takeChild(view);

    presenterService.dropView(presenterService.currentParentId, false);

    Assert.assertEquals(presenterService.presenters.size(), 0);
  }

  @Test
  public void testTakeView_oldPresentersChildrenNotCleared() {
    MVPView view = mock(MVPView.class);
    MVPView view2 = mock(MVPView.class);
    Presenter presenter = mock(Presenter.class);
    Presenter presenter2 = mock(Presenter.class);
    when(presenterFactory.createPresenter(view)).thenReturn(presenter);
    when(presenterFactory.createPresenter(view2)).thenReturn(presenter2);

    //create the presenter
    presenterService.takeView(view, null);

    String id = presenterService.currentParentId;
    when(bundle.getString(PresenterServiceImpl.PRESENTER_ID_KEY)).thenReturn(id);
    when(bundle.containsKey(PresenterServiceImpl.PRESENTER_ID_KEY)).thenReturn(true);

    //add children
    presenterService.presenters.put("1", new PresenterServiceImpl.PresenterNode(mock(Presenter.class)));
    presenterService.presenters.put("2", new PresenterServiceImpl.PresenterNode(mock(Presenter.class)));
    presenterService.presenters.put("3", new PresenterServiceImpl.PresenterNode(mock(Presenter.class)));

    //don't create the presenter again, should be the same presenter
    presenterService.takeView(view2, bundle);

    Assert.assertTrue(presenterService.presenters.containsKey("1"));
    Assert.assertTrue(presenterService.presenters.containsKey("2"));
    Assert.assertTrue(presenterService.presenters.containsKey("3"));
  }
}
