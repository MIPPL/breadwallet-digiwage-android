package com.wagerrwallet.tools.manager;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.WorkerThread;
import android.support.constraint.solver.widgets.Rectangle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.platform.HTTPServer;
import com.wagerrwallet.R;
import com.wagerrwallet.presenter.activities.EventsActivity;
import com.wagerrwallet.presenter.activities.WalletActivity;
import com.wagerrwallet.presenter.activities.settings.WebViewActivity;
import com.wagerrwallet.presenter.customviews.BRDialogView;
import com.wagerrwallet.presenter.entities.EventTxUiHolder;
import com.wagerrwallet.presenter.entities.TxUiHolder;
import com.wagerrwallet.presenter.fragments.FragmentRequestAmount;
import com.wagerrwallet.presenter.fragments.FragmentSend;
import com.wagerrwallet.presenter.fragments.FragmentWebView;
import com.wagerrwallet.tools.adapter.EventListAdapter;
import com.wagerrwallet.tools.adapter.TransactionListAdapter;
import com.wagerrwallet.tools.animation.BRAnimator;
import com.wagerrwallet.tools.animation.BRDialog;
import com.wagerrwallet.tools.listeners.RecyclerItemClickListener;
import com.wagerrwallet.wallet.WalletsMaster;
import com.wagerrwallet.wallet.abstracts.BaseWalletManager;
import com.wagerrwallet.wallet.wallets.wagerr.WalletWagerrManager;

import java.util.Date;
import java.util.List;


/**
 * BreadWalletP
 * <p/>
 * Created by Mihail Gutan on <mihail@breadwallet.com> 7/19/17.
 * Copyright (c) 2017 breadwallet LLC
 * <p/>
 *
 * (c) Wagerr Betting platform 2019
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
public class EventTxManager {

    private static final String TAG = EventTxManager.class.getName();
    private static EventTxManager instance;
    private RecyclerView txList;
    public EventListAdapter adapter;

    public static EventTxManager getInstance() {
        if (instance == null) instance = new EventTxManager();
        return instance;
    }

    public void init(final EventsActivity app) {
        txList = app.findViewById(R.id.tx_list);
        txList.setLayoutManager(new CustomLinearLayoutManager(app));
        //txList.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));
        txList.addOnItemTouchListener(new RecyclerItemClickListener(app,
                txList, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, float x, float y) {
                try {
                    EventTxUiHolder item = adapter.getItems().get(position);
                    Date date = new Date();
                    long timeStampLimit = (date.getTime()/1000) + WalletWagerrManager.BET_CUTTOFF_SECONDS;
                    if (item.getEventTimestamp()<timeStampLimit)    {
                        BRDialog.showCustomDialog(app, "Error", "Event is closed for betting", app.getString(R.string.Button_ok), null, new BRDialogView.BROnClickListener() {
                            @Override
                            public void onClick(BRDialogView brDialogView) {
                                brDialogView.dismiss();
                            }
                        }, null, null, 0);
                    }

                    int[] screenPos = new int[2];
                    ImageButton btn = (ImageButton) ((RelativeLayout) view).getChildAt(1);
                    RecyclerView parent = ((RecyclerView)view.getParent());
                    btn.getLocationOnScreen(screenPos);
                    Rectangle buttonRect = new Rectangle();
                    buttonRect.x = screenPos[0];
                    buttonRect.y = screenPos[1];
                    buttonRect.width = btn.getWidth();
                    buttonRect.height = btn.getHeight();
                    parent.getLocationOnScreen(screenPos);
                    float scrollY = parent.computeVerticalScrollOffset();
                    int nx = (int) (x + screenPos[0]);
                    int ny = (int) (y + screenPos[1]);

                    Boolean isBetSmartClick = buttonRect.contains( nx, ny );

                    BaseWalletManager wallet = WalletsMaster.getInstance(app).getCurrentWallet(app);
                    Boolean isSyncing =  wallet.getPeerManager().getSyncProgress(BRSharedPrefs.getStartHeight(app, "WGR" ))<1;
                    //isSyncing = false;  // +++ temp for testing
                    if (isSyncing)    {
                        BRDialog.showCustomDialog(app, "Error", "Wallet is still syncing", app.getString(R.string.Button_ok), null, new BRDialogView.BROnClickListener() {
                            @Override
                            public void onClick(BRDialogView brDialogView) {
                                brDialogView.dismiss();
                            }
                        }, null, null, 0);
                    }
                    else {
                        //Toast.makeText(app, "tapped: by=" + buttonRect.y + ", ny=" + ny , Toast.LENGTH_LONG).show();
                        if (isBetSmartClick)    {
                            adapter.CreateWebFragment( app, String.format("https://betsmart.app/teaser-event?id=%d&mode=light&source=wagerr", item.getEventID()));
                        }
                        else    {
                            BRAnimator.showEventDetails(app, item, position);
                        }
                    }
                }
                catch (ArrayIndexOutOfBoundsException e)    {

                }
            }

            @Override
            public void onLongItemClick(View view, int position) {

            }
        }));
        if (adapter == null)
            adapter = new EventListAdapter(app, null);
        txList.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        //setupSwipe(app);
    }

    private EventTxManager() {
    }

    public void onResume(final Activity app) {
        crashIfNotMain();
    }

    @WorkerThread
    public synchronized void updateTxList(final Context app) {
        long start = System.currentTimeMillis();
        BaseWalletManager wallet = WalletsMaster.getInstance(app).getCurrentWallet(app);
        if (wallet == null) {
            Log.e(TAG, "updateTxList: wallet is null");
            return;
        }
        final List<EventTxUiHolder> betItems = wallet.getEventTxUiHolders(app);

        long took = (System.currentTimeMillis() - start);
        if (took > 500)
            Log.e(TAG, "updateEventList: took: " + took);
        if (adapter != null && !((EventsActivity)app).isSearchActive()) {
            ((Activity) app).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (adapter!=null && betItems!=null) {
                        final List<EventTxUiHolder> currentItems = adapter.getItems();
                        currentItems.clear();
                        for (EventTxUiHolder item : betItems) {
                            currentItems.add(item);
                        }
                        adapter.setItems(currentItems);
                        //adapter.setItems(betItems);
                        //txList.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                    }
                }
            });
        }

    }

    private class CustomLinearLayoutManager extends LinearLayoutManager {

        public CustomLinearLayoutManager(Context context) {
            super(context);
        }

        /**
         * Disable predictive animations. There is a bug in RecyclerView which causes views that
         * are being reloaded to pull invalid ViewHolders from the internal recycler stack if the
         * adapter size has decreased since the ViewHolder was recycled.
         */
        @Override
        public boolean supportsPredictiveItemAnimations() {
            return false;
        }

        public CustomLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
            super(context, orientation, reverseLayout);
        }

        public CustomLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
        }
    }

    private void crashIfNotMain() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new IllegalAccessError("Can only call from main thread");
        }
    }

}
