package com.wagerrwallet.presenter.activities;

import android.animation.LayoutTransition;
import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.ConsumerIrManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.transition.TransitionManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ViewFlipper;

import com.wagerrwallet.BuildConfig;
import com.wagerrwallet.R;
import com.wagerrwallet.core.BRCorePeer;
import com.wagerrwallet.core.BRCoreTransaction;
import com.wagerrwallet.presenter.activities.util.BRActivity;
import com.wagerrwallet.presenter.customviews.BRButton;
import com.wagerrwallet.presenter.customviews.BRDialogView;
import com.wagerrwallet.presenter.customviews.BRDiceSearchBar;
import com.wagerrwallet.presenter.customviews.BRNotificationBar;
import com.wagerrwallet.presenter.customviews.BRText;
import com.wagerrwallet.presenter.entities.BetEntity;
import com.wagerrwallet.presenter.entities.BetEventEntity;
import com.wagerrwallet.presenter.entities.BetQuickGamesEntity;
import com.wagerrwallet.presenter.entities.CryptoRequest;
import com.wagerrwallet.tools.animation.BRAnimator;
import com.wagerrwallet.tools.animation.BRDialog;
import com.wagerrwallet.tools.manager.BRSharedPrefs;
import com.wagerrwallet.tools.manager.DiceManager;
import com.wagerrwallet.tools.manager.FontManager;
import com.wagerrwallet.tools.manager.InternetManager;
import com.wagerrwallet.tools.manager.DiceManager;
import com.wagerrwallet.tools.manager.SendManager;
import com.wagerrwallet.tools.manager.SyncManager;
import com.wagerrwallet.tools.sqlite.CurrencyDataSource;
import com.wagerrwallet.tools.threads.executor.BRExecutor;
import com.wagerrwallet.tools.util.CurrencyUtils;
import com.wagerrwallet.tools.util.Utils;
import com.wagerrwallet.wallet.WalletsMaster;
import com.wagerrwallet.wallet.abstracts.BaseWalletManager;
import com.wagerrwallet.wallet.abstracts.SyncListener;
import com.wagerrwallet.wallet.wallets.util.CryptoUriParser;

import java.math.BigDecimal;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import static com.wagerrwallet.tools.animation.BRAnimator.t1Size;
import static com.wagerrwallet.tools.animation.BRAnimator.t2Size;

/**
 * Created by MIP on 11/14/20.
 * Copyright (c) 2020 Wagerr LTD
 * <p>
 *
 * (c) Wagerr Betting platform 2020
 */

public class DiceActivity extends BRActivity implements InternetManager.ConnectionReceiverListener, SyncManager.OnProgressUpdate {
    private static final String TAG = DiceActivity.class.getName();
    private static final long UNIT_MULTIPLIER = 100000000L;     // so far in full WGR units

    BRText mCurrencyTitle;
    BRText mCurrencyPriceUsd;
    BRText mBalancePrimary;
    BRText mBalanceSecondary;
    Toolbar mToolbar;
    ImageButton mBackButton;
    BRText mBalanceLabel;
    BRText mProgressLabel;
    ProgressBar mProgressBar;

    public ViewFlipper barFlipper;
    private BRDiceSearchBar searchBar;
    private ImageButton mSearchIcon;
    private ImageButton mSwap;
    private ConstraintLayout toolBarConstraintLayout;
    private ConstraintLayout toolBarConstraintLayout2;

    private BRNotificationBar mNotificationBar;

    private static DiceActivity app;

    // dice bet controls
    LinearLayout mLayoutBetOptions;
    Button mEqualNotEqual;
    Button mOverUnder;
    Button mEvenOdds;

    Button[] mDiceN = new Button[11];
    LinearLayout mLayoutDiceEqualNotEqual;
    LinearLayout mLayoutDiceEqualNotEqual2;

    Button[] mDiceN5 = new Button[10];
    LinearLayout mLayoutDiceOverUnder;
    LinearLayout mLayoutDiceOverUnder2;

    Button mBetLeft;
    Button mBetRight;
    EditText mBetAmount;

    // switches
    private enum DiceBetOptions {
        DICE_BET_EQUAL_NOT_EQUAL,
        DICE_BET_OVER_UNDER,
        DICE_BET_EVEN_ODDS
    }

    private DiceBetOptions diceBetOptions = DiceBetOptions.DICE_BET_EQUAL_NOT_EQUAL;
    private int nDiceNSelected = 0;
    private int nDiceN5Selected = 0;
    private int nDiceNSelectedPrev = 0;
    private int nDiceN5SelectedPrev = 0;

    private InternetManager mConnectionReceiver;
    private TestLogger logger;
    public boolean isSearchBarVisible = false;

    public static DiceActivity getApp() {
        return app;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_dice);

        mCurrencyTitle = findViewById(R.id.currency_label);
        mCurrencyPriceUsd = findViewById(R.id.currency_usd_price);
        mBalancePrimary = findViewById(R.id.balance_primary);
        mBalanceSecondary = findViewById(R.id.balance_secondary);
        mToolbar = findViewById(R.id.bread_bar);
        mBackButton = findViewById(R.id.back_icon);
        barFlipper = findViewById(R.id.tool_bar_flipper);
        searchBar = findViewById(R.id.search_bar);
        mSearchIcon = findViewById(R.id.search_icon);
        toolBarConstraintLayout = findViewById(R.id.bread_toolbar);
        toolBarConstraintLayout2= findViewById(R.id.bread_toolbar2);
        mSwap = findViewById(R.id.swap);
        mBalanceLabel = findViewById(R.id.balance_label);
        mProgressLabel = findViewById(R.id.syncing_label);
        mProgressBar = findViewById(R.id.sync_progress);
        mNotificationBar = findViewById(R.id.notification_bar);

        mLayoutBetOptions = findViewById(R.id.layout_bet_options);
        mEqualNotEqual = findViewById(R.id.dice_eqnoteq);
        mOverUnder = findViewById(R.id.dice_overunder);
        mEvenOdds = findViewById(R.id.dice_evenodds);

        mEqualNotEqual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                diceBetOptions = DiceBetOptions.DICE_BET_EQUAL_NOT_EQUAL;
                updateDiceButtonsUI();
            }
        });

        mOverUnder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                diceBetOptions = DiceBetOptions.DICE_BET_OVER_UNDER;
                updateDiceButtonsUI();
            }
        });

        mEvenOdds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                diceBetOptions = DiceBetOptions.DICE_BET_EVEN_ODDS;
                updateDiceButtonsUI();
            }
        });

        mLayoutDiceEqualNotEqual = findViewById(R.id.layout_dice_equal);
        mLayoutDiceEqualNotEqual2 = findViewById(R.id.layout_dice_equal2);
        mDiceN[0] = findViewById(R.id.dice_2);
        mDiceN[1] = findViewById(R.id.dice_3);
        mDiceN[2] = findViewById(R.id.dice_4);
        mDiceN[3] = findViewById(R.id.dice_5);
        mDiceN[4] = findViewById(R.id.dice_6);
        mDiceN[5] = findViewById(R.id.dice_7);
        mDiceN[6] = findViewById(R.id.dice_8);
        mDiceN[7] = findViewById(R.id.dice_9);
        mDiceN[8] = findViewById(R.id.dice_10);
        mDiceN[9] = findViewById(R.id.dice_11);
        mDiceN[10] = findViewById(R.id.dice_12);

        int n=0;
        for ( Button btn : mDiceN)  {
            final int i = n;
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    nDiceNSelected = i;
                    if (nDiceNSelectedPrev != nDiceNSelected) {
                        updateDiceButtonsUI();
                        nDiceNSelectedPrev = nDiceNSelected;
                    }
                }
            });
            n++;
        }

        mLayoutDiceOverUnder = findViewById(R.id.layout_dice_overunder);
        mLayoutDiceOverUnder2 = findViewById(R.id.layout_dice_overunder2);
        mDiceN5[0] = findViewById(R.id.dice_2_5);
        mDiceN5[1] = findViewById(R.id.dice_3_5);
        mDiceN5[2] = findViewById(R.id.dice_4_5);
        mDiceN5[3] = findViewById(R.id.dice_5_5);
        mDiceN5[4] = findViewById(R.id.dice_6_5);
        mDiceN5[5] = findViewById(R.id.dice_7_5);
        mDiceN5[6] = findViewById(R.id.dice_8_5);
        mDiceN5[7] = findViewById(R.id.dice_9_5);
        mDiceN5[8] = findViewById(R.id.dice_10_5);
        mDiceN5[9] = findViewById(R.id.dice_11_5);

        n = 0;
        for ( Button btn : mDiceN5)  {
            final int i = n;
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    nDiceN5Selected = i;
                    if (nDiceN5SelectedPrev != nDiceN5Selected) {
                        updateDiceButtonsUI();
                        nDiceN5SelectedPrev = nDiceN5Selected;
                    }
                }
            });
            n++;
        }

        mBetLeft = findViewById(R.id.dice_bet_left);

        mBetLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {    AcceptBet( true );            }
        });

        mBetRight = findViewById(R.id.dice_bet_right);
        mBetRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {    AcceptBet( false );           }
        });

        mBetAmount = findViewById(R.id.tx_amount);

        if (Utils.isEmulatorOrDebug(this)) {
            if (logger != null) logger.interrupt();
            logger = new TestLogger(); //Sync logger
            logger.start();
        }

        setUpBarFlipper();

        BRAnimator.init(this);
        mBalancePrimary.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);//make it the size it should be after animation to get the X
        mBalanceSecondary.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);//make it the size it should be after animation to get the X

        BRAnimator.init(this);
        mBalancePrimary.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);//make it the size it should be after animation to get the X
        mBalanceSecondary.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);//make it the size it should be after animation to get the X

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
                finish();
            }
        });

        mSearchIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!BRAnimator.isClickAllowed()) return;
                barFlipper.setDisplayedChild(1); //search bar
                searchBar.onShow(true);
            }
        });

        mBalancePrimary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swap();
            }
        });
        mBalanceSecondary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swap();
            }
        });

        DiceManager.getInstance().init(this);

        onConnectionChanged(InternetManager.getInstance().isConnected(this));

        updateUi();

        boolean cryptoPreferred = BRSharedPrefs.isCryptoPreferred(this);

        if (cryptoPreferred) {
            setPriceTags(cryptoPreferred, false);
            //swap();   // buggy when restoring activity
        }

        // Check if the "Twilight" screen altering app is currently running
        if (checkIfScreenAlteringAppIsRunning("com.urbandroid.lux")) {
            BRDialog.showSimpleDialog(this, getString(R.string.Dialog_screenAlteringTitle), getString(R.string.Dialog_screenAlteringMessage));
        }
        
    }

    private void AcceptBet( boolean bLeft )    {
        BaseWalletManager wallet = WalletsMaster.getInstance(app).getCurrentWallet(app);
        Boolean isSyncing =  wallet.getPeerManager().getSyncProgress(BRSharedPrefs.getStartHeight(app, "WGR" ))<1;
        isSyncing = false;  // +++ temp for testing
        String strErrMessage = "";
        if (isSyncing)    {
            strErrMessage = "Please wait until wallet is fully synced";
        }
        else {
            int min = app.getResources().getInteger(R.integer.min_bet_amount);
            int max = Math.min( (int)(wallet.getWallet().getBalance()/UNIT_MULTIPLIER),
                    app.getResources().getInteger(R.integer.max_bet_amount));
            String strAmount = mBetAmount.getText().toString().replace(',', '.');
            Float fAmount = Float.parseFloat(strAmount);
            long amount = (long)(fAmount * UNIT_MULTIPLIER);

            if ( fAmount < min )    {
                strErrMessage = String.format("Minimum bet amount is %d WGR", min );
            }
            if (fAmount > max)  {
                strErrMessage = String.format("Maximum bet amount is %d WGR", max );
            }

            if ( strErrMessage =="" ) {  // no errors, continue
                BRCoreTransaction tx = wallet.getWallet().createDiceBetTransaction(amount, BetQuickGamesEntity.BetQuickGameType.DICE.getNumber(), getDiceGameType(bLeft).getNumber(), getSelectedOutcome());
                CryptoRequest item = new CryptoRequest(tx, null, false, "", "", new BigDecimal(amount));
                SendManager.sendTransaction(DiceActivity.this, item, wallet);
            }
        }

        if ( strErrMessage!="" ) {
            BRDialog.showCustomDialog(app, "Error", strErrMessage, app.getString(R.string.Button_ok), null, new BRDialogView.BROnClickListener() {
                @Override
                public void onClick(BRDialogView brDialogView) {
                    brDialogView.dismiss();
                }
            }, null, null, 0);
        }
    }

    private BetQuickGamesEntity.BetDiceGameType getDiceGameType( boolean bLeft)   {
        BetQuickGamesEntity.BetDiceGameType ret = BetQuickGamesEntity.BetDiceGameType.UNKNOWN;

        switch ( diceBetOptions )   {
            case DICE_BET_EQUAL_NOT_EQUAL:
                ret = (bLeft) ? BetQuickGamesEntity.BetDiceGameType.EQUAL: BetQuickGamesEntity.BetDiceGameType.NOT_EQUAL ;
                break;

            case DICE_BET_OVER_UNDER:
                ret = (bLeft) ? BetQuickGamesEntity.BetDiceGameType.TOTAL_OVER: BetQuickGamesEntity.BetDiceGameType.TOTAL_UNDER ;
                break;

            case DICE_BET_EVEN_ODDS:
                ret = (bLeft) ? BetQuickGamesEntity.BetDiceGameType.EVEN: BetQuickGamesEntity.BetDiceGameType.ODDS ;
                break;
        }
        return ret;
    }

    private int getSelectedOutcome()    {
        int ret = 0;
        switch ( diceBetOptions )   {
            case DICE_BET_EQUAL_NOT_EQUAL:
                ret = nDiceNSelected+2;
                break;

            case DICE_BET_OVER_UNDER:
                ret = nDiceN5Selected+1;
                break;

            case DICE_BET_EVEN_ODDS:    // no outcome, ignored
                break;
        }
        return ret;
    }

    public boolean isSearchActive() {
        return isSearchBarVisible;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mConnectionReceiver != null)
            unregisterReceiver(mConnectionReceiver);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        //since we have one instance of activity at all times, this is needed to know when a new intent called upon this activity
        handleUrlClickIfNeeded(intent);
    }

    private void handleUrlClickIfNeeded(Intent intent) {
        Uri data = intent.getData();
        if (data != null && !data.toString().isEmpty()) {
            //handle external click with crypto scheme
            CryptoUriParser.processRequest(this, data.toString(), WalletsMaster.getInstance(this).getCurrentWallet(this));
        }
    }

    private void updateDiceButtonsUI()  {
        boolean bDiceEqualVisible = false;
        boolean bDiceOverVisible = false;
        Drawable drwSelected = getResources().getDrawable( R.drawable.dice_button_bg_selected );
        Drawable drwNotSelected = getResources().getDrawable( R.drawable.dice_button_bg );

        switch ( diceBetOptions )   {
            case DICE_BET_EQUAL_NOT_EQUAL:
                bDiceEqualVisible = true;
                mEqualNotEqual.setBackground( drwSelected );
                mOverUnder.setBackground( drwNotSelected );
                mEvenOdds.setBackground( drwNotSelected );
                mDiceN[nDiceNSelectedPrev].setBackground( drwNotSelected );
                mDiceN[nDiceNSelected].setBackground( drwSelected );
                mBetLeft.setText( getResources().getString( R.string.Dice_EqualTo) );
                mBetRight.setText( getResources().getString( R.string.Dice_NotEqualTo) );
                break;

            case DICE_BET_OVER_UNDER:
                bDiceOverVisible = true;
                mEqualNotEqual.setBackground( drwNotSelected );
                mOverUnder.setBackground( drwSelected );
                mEvenOdds.setBackground( drwNotSelected );
                mDiceN5[nDiceN5SelectedPrev].setBackground( drwNotSelected );
                mDiceN5[nDiceN5Selected].setBackground( drwSelected );
                mBetLeft.setText( getResources().getString( R.string.Dice_RollOver) );
                mBetRight.setText( getResources().getString( R.string.Dice_RollUnder) );
                break;

            case DICE_BET_EVEN_ODDS:
                mEqualNotEqual.setBackground( drwNotSelected );
                mOverUnder.setBackground( drwNotSelected );
                mEvenOdds.setBackground( drwSelected );
                mBetLeft.setText( getResources().getString( R.string.Dice_RollEven) );
                mBetRight.setText( getResources().getString( R.string.Dice_RollOdds) );
                break;
        }
        mLayoutDiceEqualNotEqual.setVisibility((bDiceEqualVisible)?View.VISIBLE:View.GONE);
        mLayoutDiceEqualNotEqual2.setVisibility((bDiceEqualVisible)?View.VISIBLE:View.GONE);
        mLayoutDiceOverUnder.setVisibility((bDiceOverVisible)?View.VISIBLE:View.GONE);
        mLayoutDiceOverUnder2.setVisibility((bDiceOverVisible)?View.VISIBLE:View.GONE);
    }

    private void updateUi() {
        final BaseWalletManager wallet = WalletsMaster.getInstance(this).getCurrentWallet(this);
        if (wallet == null) {
            Log.e(TAG, "updateUi: wallet is null");
            return;
        }

//        String fiatIso = BRSharedPrefs.getPreferredFiatIso(this);

        String fiatExchangeRate = CurrencyUtils.getFormattedAmount(this, BRSharedPrefs.getPreferredFiatIso(this), wallet.getFiatExchangeRate(this));
        String fiatBalance = CurrencyUtils.getFormattedAmount(this, BRSharedPrefs.getPreferredFiatIso(this), wallet.getFiatBalance(this));
        String cryptoBalance = CurrencyUtils.getFormattedAmount(this, wallet.getIso(this), new BigDecimal(wallet.getCachedBalance(this)));

        mCurrencyTitle.setText(wallet.getName(this));
        mCurrencyPriceUsd.setText(String.format("%s per %s", fiatExchangeRate, wallet.getIso(this)));
        mBalancePrimary.setText(fiatBalance);
        mBalanceSecondary.setText(cryptoBalance);
        mToolbar.setBackgroundColor(Color.parseColor(wallet.getUiConfiguration().colorHex));
        toolBarConstraintLayout2.setBackgroundColor(Color.parseColor(wallet.getUiConfiguration().colorHex));

        BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
                DiceManager.getInstance().updateDiceList(DiceActivity.this);
            }
        });

    }

    // This method checks if a screen altering app(such as Twightlight) is currently running
    // If it is, notify the user that the BRD app will not function properly and they should
    // disable it
    private boolean checkIfScreenAlteringAppIsRunning(String packageName) {

        // Use the ActivityManager API if sdk version is less than 21
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            // Get the Activity Manager
            ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

            // Get a list of running tasks, we are only interested in the last one,
            // the top most so we give a 1 as parameter so we only get the topmost.
            List<ActivityManager.RunningAppProcessInfo> processes = manager.getRunningAppProcesses();
            Log.d(TAG, "Process list count -> " + processes.size());


            String processName = "";
            for (ActivityManager.RunningAppProcessInfo processInfo : processes) {

                // Get the info we need for comparison.
                processName = processInfo.processName;
                Log.d(TAG, "Process package name -> " + processName);

                // Check if it matches our package name
                if (processName.equals(packageName)) return true;


            }


        }
        // Use the UsageStats API for sdk versions greater than Lollipop
        else {
            UsageStatsManager usm = (UsageStatsManager) this.getSystemService(USAGE_STATS_SERVICE);
            long time = System.currentTimeMillis();
            List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 1000, time);
            if (appList != null && appList.size() > 0) {
                SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
                String currentPackageName = "";
                for (UsageStats usageStats : appList) {
                    mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                    currentPackageName = usageStats.getPackageName();


                    if (currentPackageName.equals(packageName)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private void swap() {
        if (!BRAnimator.isClickAllowed()) return;
        boolean b = !BRSharedPrefs.isCryptoPreferred(this);
        setPriceTags(b, true);
        BRSharedPrefs.setIsCryptoPreferred(this, b);
    }

    private void setPriceTags(final boolean cryptoPreferred, boolean animate) {
        //mBalanceSecondary.setTextSize(!cryptoPreferred ? t1Size : t2Size);
        //mBalancePrimary.setTextSize(!cryptoPreferred ? t2Size : t1Size);
        ConstraintSet set = new ConstraintSet();
        set.clone(toolBarConstraintLayout);
        if (animate)
            TransitionManager.beginDelayedTransition(toolBarConstraintLayout);
        int px8 = Utils.getPixelsFromDps(this, 8);
        int px16 = Utils.getPixelsFromDps(this, 16);
//
//        //align first item to parent right
//        set.connect(!cryptoPreferred ? R.id.balance_secondary : R.id.balance_primary, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, px16);
//        //align swap symbol after the first item
//        set.connect(R.id.swap, ConstraintSet.START, !cryptoPreferred ? R.id.balance_secondary : R.id.balance_primary, ConstraintSet.START, px8);
//        //align second item after swap symbol
//        set.connect(!cryptoPreferred ? R.id.balance_secondary : R.id.balance_primary, ConstraintSet.START, mSwap.getId(), ConstraintSet.END, px8);
//

        // CRYPTO on RIGHT
        if (cryptoPreferred) {

            // Align crypto balance to the right parent
            set.connect(R.id.balance_secondary, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, px8);
            set.connect(R.id.balance_secondary, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, px8);

            // Align swap icon to left of crypto balance
            set.connect(R.id.swap, ConstraintSet.END, R.id.balance_secondary, ConstraintSet.START, px8);

            // Align usd balance to left of swap icon
            set.connect(R.id.balance_primary, ConstraintSet.END, R.id.swap, ConstraintSet.START, px8);

            mBalancePrimary.setPadding(0, 0, 0, Utils.getPixelsFromDps(this, 6));
            mBalanceSecondary.setPadding(0, 0, 0, Utils.getPixelsFromDps(this, 4));
            mSwap.setPadding(0, 0, 0, Utils.getPixelsFromDps(this, 2));

            Log.d(TAG, "CryptoPreferred " + cryptoPreferred);

            mBalanceSecondary.setTextSize(t1Size);
            mBalancePrimary.setTextSize(t2Size);

            set.applyTo(toolBarConstraintLayout);

        }

        // CRYPTO on LEFT
        else {

            // Align primary to right of parent
            set.connect(R.id.balance_primary, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, px8);

            // Align swap icon to left of usd balance
            set.connect(R.id.swap, ConstraintSet.END, R.id.balance_primary, ConstraintSet.START, px8);


            // Align secondary currency to the left of swap icon
            set.connect(R.id.balance_secondary, ConstraintSet.END, R.id.swap, ConstraintSet.START, px8);

            mBalancePrimary.setPadding(0, 0, 0, Utils.getPixelsFromDps(this, 2));
            mBalanceSecondary.setPadding(0, 0, 0, Utils.getPixelsFromDps(this, 4));
            mSwap.setPadding(0, 0, 0, Utils.getPixelsFromDps(this, 2));

            //mBalancePrimary.setPadding(0,0, 0, Utils.getPixelsFromDps(this, -4));

            Log.d(TAG, "CryptoPreferred " + cryptoPreferred);

            mBalanceSecondary.setTextSize(t2Size);
            mBalancePrimary.setTextSize(t1Size);


            set.applyTo(toolBarConstraintLayout);

        }


        if (!cryptoPreferred) {
            mBalanceSecondary.setTextColor(getResources().getColor(R.color.currency_subheading_color, null));
            mBalancePrimary.setTextColor(getResources().getColor(R.color.white, null));
            mBalanceSecondary.setTypeface(FontManager.get(this, "CircularPro-Book.otf"));

        } else {
            mBalanceSecondary.setTextColor(getResources().getColor(R.color.white, null));
            mBalancePrimary.setTextColor(getResources().getColor(R.color.currency_subheading_color, null));
            mBalanceSecondary.setTypeface(FontManager.get(this, "CircularPro-Bold.otf"));

        }

        new Handler().postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        updateUi();
                    }
                }, toolBarConstraintLayout.getLayoutTransition().getDuration(LayoutTransition.CHANGE_APPEARING));
    }

    @Override
    protected void onResume() {
        super.onResume();

        app = this;

        WalletsMaster.getInstance(app).initWallets(app);

        setupNetworking();

        DiceManager.getInstance().adapter.updateData();
        DiceManager.getInstance().onResume(this);

        CurrencyDataSource.getInstance(this).addOnDataChangedListener(new CurrencyDataSource.OnDataChanged() {
            @Override
            public void onChanged() {
                BRExecutor.getInstance().forMainThreadTasks().execute(new Runnable() {
                    @Override
                    public void run() {
                        updateUi();
                    }
                });
            }
        });
        final BaseWalletManager wallet = WalletsMaster.getInstance(this).getCurrentWallet(this);
        BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
                long balance = wallet.getWallet().getBalance();
                wallet.setCashedBalance(app, balance);
                BRExecutor.getInstance().forMainThreadTasks().execute(new Runnable() {
                    @Override
                    public void run() {
                        updateUi();
                    }
                });

            }
        });

        BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
                if (wallet.getPeerManager().getConnectStatus() != BRCorePeer.ConnectStatus.Connected)
                    wallet.connectWallet(DiceActivity.this);
            }
        });

        wallet.addSyncListeners(new SyncListener() {
            @Override
            public void syncStopped(String err) {

            }

            @Override
            public void syncStarted() {
                SyncManager.getInstance().startSyncing(DiceActivity.this, wallet, DiceActivity.this);
            }
        });

        SyncManager.getInstance().startSyncing(this, wallet, this);

        handleUrlClickIfNeeded(getIntent());
    }

    @Override
    protected void onPause() {
        super.onPause();
        SyncManager.getInstance().stopSyncing();
    }

    private void setUpBarFlipper() {
        barFlipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.flipper_enter));
        barFlipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.flipper_exit));
    }

    public void resetFlipper() {
        barFlipper.setDisplayedChild(0);
    }

    private void setupNetworking() {
        if (mConnectionReceiver == null) mConnectionReceiver = InternetManager.getInstance();
        IntentFilter mNetworkStateFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mConnectionReceiver, mNetworkStateFilter);
        InternetManager.addConnectionListener(this);
    }


    @Override
    public void onConnectionChanged(boolean isConnected) {
        Log.d(TAG, "onConnectionChanged");
        if (isConnected) {
            if (barFlipper != null && barFlipper.getDisplayedChild() == 2) {
                barFlipper.setDisplayedChild(0);
            }
            final BaseWalletManager wm = WalletsMaster.getInstance(DiceActivity.this).getCurrentWallet(DiceActivity.this);
            BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
                @Override
                public void run() {
                    final double progress = wm.getPeerManager()
                            .getSyncProgress(BRSharedPrefs.getStartHeight(DiceActivity.this,
                                    BRSharedPrefs.getCurrentWalletIso(DiceActivity.this)));
//                    Log.e(TAG, "run: " + progress);
                    if (progress < 1 && progress > 0) {
                        SyncManager.getInstance().startSyncing(DiceActivity.this, wm, DiceActivity.this);
                    }
                }
            });

        } else {
            if (barFlipper != null)
                barFlipper.setDisplayedChild(2);

        }
    }


    @Override
    public void onBackPressed() {
        int c = getFragmentManager().getBackStackEntryCount();
        if (c > 0) {
            super.onBackPressed();
            return;
        }
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
        if (!isDestroyed()) {
            finish();
        }
    }

    @Override
    public boolean onProgressUpdated(double progress) {
        mProgressBar.setProgress((int) (progress * 100));
        if (progress == 1) {
            mProgressBar.setVisibility(View.GONE);
            mProgressLabel.setVisibility(View.GONE);
            mBalanceLabel.setVisibility(View.VISIBLE);
            mProgressBar.invalidate();
            return false;
        }
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressLabel.setVisibility(View.VISIBLE);
        mBalanceLabel.setVisibility(View.GONE);
        mProgressBar.invalidate();
        return true;
    }

    //test logger
    class TestLogger extends Thread {
        private static final String TAG = "TestLogger";

        @Override
        public void run() {
            super.run();

            while (true) {
                StringBuilder builder = new StringBuilder();
                for (BaseWalletManager w : WalletsMaster.getInstance(DiceActivity.this).getAllWallets()) {
                    builder.append("   " + w.getIso(DiceActivity.this));
                    String connectionStatus = "";
                    if (w.getPeerManager().getConnectStatus() == BRCorePeer.ConnectStatus.Connected)
                        connectionStatus = "Connected";
                    else if (w.getPeerManager().getConnectStatus() == BRCorePeer.ConnectStatus.Disconnected)
                        connectionStatus = "Disconnected";
                    else if (w.getPeerManager().getConnectStatus() == BRCorePeer.ConnectStatus.Connecting)
                        connectionStatus = "Connecting";

                    double progress = w.getPeerManager().getSyncProgress(BRSharedPrefs.getStartHeight(DiceActivity.this, w.getIso(DiceActivity.this)));

                    builder.append(" - " + connectionStatus + " " + progress * 100 + "%     ");

                }

                Log.e(TAG, "testLog: " + builder.toString());

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }

}
