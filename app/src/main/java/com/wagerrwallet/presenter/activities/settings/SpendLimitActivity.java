package com.wagerrwallet.presenter.activities.settings;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;

import com.wagerrwallet.R;
import com.wagerrwallet.presenter.activities.util.BRActivity;
import com.wagerrwallet.presenter.customviews.BRText;
import com.wagerrwallet.tools.animation.BRAnimator;
import com.wagerrwallet.tools.security.AuthManager;
import com.wagerrwallet.tools.security.BRKeyStore;
import com.wagerrwallet.tools.util.BRConstants;
import com.wagerrwallet.tools.util.CurrencyUtils;
import com.wagerrwallet.wallet.WalletsMaster;
import com.wagerrwallet.wallet.abstracts.BaseWalletManager;
import com.wagerrwallet.wallet.wallets.wagerr.WalletWagerrManager;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;


import static com.wagerrwallet.tools.util.BRConstants.ONE_BITCOIN;


public class SpendLimitActivity extends BRActivity {
    private static final String TAG = SpendLimitActivity.class.getName();
    public static boolean appVisible = false;
    private static SpendLimitActivity app;
    private ListView listView;
    private LimitAdaptor adapter;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
    }

    public static SpendLimitActivity getApp() {
        return app;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spend_limit);

        ImageButton faq = findViewById(R.id.faq_button);

        faq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!BRAnimator.isClickAllowed()) return;
                BRAnimator.showSupportFragment(app, BRConstants.fingerprintSpendingLimit);
            }
        });

        listView = findViewById(R.id.limit_list);
        listView.setFooterDividersEnabled(true);
        adapter = new LimitAdaptor(this);
        List<BigInteger> items = new ArrayList<>();
        items.add(getAmountByStep(0));
        items.add(getAmountByStep(1));
        items.add(getAmountByStep(2));
        items.add(getAmountByStep(3));
        items.add(getAmountByStep(4));

        adapter.addAll(items);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                BigInteger limit = adapter.getItem(position);
                Log.e(TAG, "limit chosen: " + limit);
                BRKeyStore.putSpendLimit(limit.longValue(), app);
                long totalSent = 0;
                List<BaseWalletManager> wallets = WalletsMaster.getInstance(app).getAllWallets();
                for (BaseWalletManager w : wallets)
                    totalSent += w.getTotalSent(app); //collect total total sent
                AuthManager.getInstance().setTotalLimit(app, totalSent + BRKeyStore.getSpendLimit(app));
                adapter.notifyDataSetChanged();
            }
        });
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

    //satoshis
    private BigInteger getAmountByStep(int step) {
        BigInteger result;
        switch (step) {
            case 0:
                result = BigInteger.valueOf(0L);// 0 always require
                break;
            case 1:
                result = BigInteger.valueOf((long)ONE_BITCOIN * 10);//   0.01 BTC
                break;
            case 2:
                result = BigInteger.valueOf((long)ONE_BITCOIN * 100);//   0.1 BTC
                break;
            case 3:
                result = BigInteger.valueOf((long)ONE_BITCOIN * 1000 );//   1 BTC
                break;
            case 4:
                result = BigInteger.valueOf((long)ONE_BITCOIN * 10000);//   10 BTC
                break;

            default:
                result = BigInteger.valueOf((long)ONE_BITCOIN*1000);//   1 BTC Default
                break;
        }
        return result;
    }

    private int getStepFromLimit(long limit) {
        int ret = 0;
        if (limit==0L ) {
            ret = 0;
        }
        else if (limit==ONE_BITCOIN *10 ) {
            ret = 1;
        }
        else if (limit==ONE_BITCOIN *100 ) {
            ret = 2;
        }
        else if (limit==ONE_BITCOIN *1000 ) {
            ret = 3;
        }
        else if (limit==ONE_BITCOIN *10000 ) {
            ret = 4;
        }
        return ret;
    }

    @Override
    protected void onResume() {
        super.onResume();
        appVisible = true;
        app = this;

    }

    @Override
    protected void onPause() {
        super.onPause();
        appVisible = false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
    }

    public class LimitAdaptor extends ArrayAdapter<BigInteger> {

        private final Context mContext;
        private final int layoutResourceId;
        private BRText textViewItem;

        public LimitAdaptor(Context mContext) {

            super(mContext, R.layout.currency_list_item);

            this.layoutResourceId = R.layout.currency_list_item;
            this.mContext = mContext;
        }

        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {

            final long limit = BRKeyStore.getSpendLimit(app);
            if (convertView == null) {
                // inflate the layout
                LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
                convertView = inflater.inflate(layoutResourceId, parent, false);
            }
            // get the TextView and then set the text (item name) and tag (item ID) values
            textViewItem = convertView.findViewById(R.id.currency_item_text);
            BigInteger item = getItem(position);
            BaseWalletManager walletManager = WalletWagerrManager.getInstance(app); //use the bitcoin wallet to show the limits

            String cryptoAmount = CurrencyUtils.getFormattedAmount(app, walletManager.getIso(app), new BigDecimal(item));

            String text = String.format(item.longValue() == 0 ? app.getString(R.string.TouchIdSpendingLimit) : "%s", cryptoAmount);
            textViewItem.setText(text);
            ImageView checkMark = convertView.findViewById(R.id.currency_checkmark);

            if (position == getStepFromLimit(limit)) {
                checkMark.setVisibility(View.VISIBLE);
            } else {
                checkMark.setVisibility(View.GONE);
            }
            return convertView;

        }

        @Override
        public int getCount() {
            return super.getCount();
        }

        @Override
        public int getItemViewType(int position) {
            return IGNORE_ITEM_VIEW_TYPE;
        }

    }

}
