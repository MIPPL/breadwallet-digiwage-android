package com.wagerrwallet.tools.sqlite;

/**
 * BreadWallet
 * <p/>
 * Created by MIP on 11/14/20.
 * Copyright (c) 2020 Wagerr LTD
 * <p>
 *
 * (c) Wagerr Betting platform 2020
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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.wagerrwallet.presenter.entities.BetEntity;
import com.wagerrwallet.presenter.entities.BetQuickGamesEntity;
import com.wagerrwallet.presenter.entities.DiceUiHolder;
import com.wagerrwallet.tools.manager.BRReportsManager;
import com.wagerrwallet.tools.util.BRConstants;

import java.util.ArrayList;
import java.util.List;

public class BetQuickGamesTxDataStore implements BRDataSourceInterface {
    private static final String TAG = BetQuickGamesTxDataStore.class.getName();

    // Database fields
    private SQLiteDatabase database;
    private final BRSQLiteHelper dbHelper;
    public static final String[] allColumns = {
            BRSQLiteHelper.BQGTX_COLUMN_ID,
            BRSQLiteHelper.BQGTX_TYPE,
            BRSQLiteHelper.BQGTX_VERSION,
            BRSQLiteHelper.BQGTX_QUICK_GAME_TYPE,
            BRSQLiteHelper.BQGTX_DICE_GAME_TYPE,
            BRSQLiteHelper.BQGTX_AMOUNT,
            BRSQLiteHelper.BQGTX_BLOCK_HEIGHT,
            BRSQLiteHelper.BQGTX_TIME_STAMP,
            BRSQLiteHelper.BQGTX_SELECTED_OUTCOME,
            BRSQLiteHelper.BQGTX_DICE1,
            BRSQLiteHelper.BQGTX_DICE2,
            BRSQLiteHelper.BQGTX_RESULT,
            BRSQLiteHelper.BQGTX_PAYOUT_AMOUNT,
            BRSQLiteHelper.BQGTX_PAYOUT_TXHASH
    };

    private static BetQuickGamesTxDataStore instance;

    public static BetQuickGamesTxDataStore getInstance(Context context) {
        if (instance == null) {
            instance = new BetQuickGamesTxDataStore(context);
        }
        return instance;
    }

    private BetQuickGamesTxDataStore(Context context) {
        dbHelper = BRSQLiteHelper.getInstance(context);
    }

    public BetQuickGamesEntity putTransaction(Context app, BetQuickGamesEntity transactionEntity) {

        Log.e(TAG, "putTransaction: :" + transactionEntity.getTxHash() + ", b:" + transactionEntity.getBlockheight() + ", t:" + transactionEntity.getTimestamp());
        Cursor cursor = null;
        try {
            database = openDatabase();
            ContentValues values = new ContentValues();
            values.put(BRSQLiteHelper.BQGTX_COLUMN_ID, transactionEntity.getTxHash());
            values.put(BRSQLiteHelper.BQGTX_TYPE, transactionEntity.getType().getNumber());
            values.put(BRSQLiteHelper.BQGTX_VERSION, transactionEntity.getVersion());
            values.put(BRSQLiteHelper.BQGTX_QUICK_GAME_TYPE, transactionEntity.getGameType().getNumber());
            values.put(BRSQLiteHelper.BQGTX_DICE_GAME_TYPE, transactionEntity.getDiceGameType().getNumber());
            values.put(BRSQLiteHelper.BQGTX_AMOUNT, transactionEntity.getAmount());
            values.put(BRSQLiteHelper.BQGTX_BLOCK_HEIGHT, transactionEntity.getBlockheight());
            values.put(BRSQLiteHelper.BQGTX_TIME_STAMP, transactionEntity.getTimestamp());
            values.put(BRSQLiteHelper.BQGTX_SELECTED_OUTCOME, transactionEntity.getSelectedOutcome());

            database.beginTransaction();
            database.insert(BRSQLiteHelper.BQGTX_TABLE_NAME, null, values);
            cursor = database.query(BRSQLiteHelper.BQGTX_TABLE_NAME,
                    allColumns, null, null, null, null, null);
            cursor.moveToFirst();
            DiceUiHolder transactionEntity1 = cursorToTransaction(app, cursor);

            database.setTransactionSuccessful();
            return transactionEntity1;
        } catch (Exception ex) {
            BRReportsManager.reportBug(ex);
            Log.e(TAG, "Error inserting bet tx into SQLite", ex);
            //Error in between database transaction
        } finally {
            database.endTransaction();
            closeDatabase();
            if (cursor != null) cursor.close();
        }
        return null;


    }

    public BetQuickGamesEntity updateTransaction(Context app, DiceUiHolder transactionEntity) {

        Log.e(TAG, "updateQGTransaction: :" + transactionEntity.getTxHash() + ", b:" + transactionEntity.getBlockheight() + ", t:" + transactionEntity.getTimestamp());
        Cursor cursor = null;
        try {
            database = openDatabase();
            ContentValues values = new ContentValues();
            values.put(BRSQLiteHelper.BQGTX_DICE1, transactionEntity.getDice1());
            values.put(BRSQLiteHelper.BQGTX_DICE2, transactionEntity.getDice2());
            values.put(BRSQLiteHelper.BQGTX_RESULT, transactionEntity.getDiceResult());
            values.put(BRSQLiteHelper.BQGTX_PAYOUT_AMOUNT, transactionEntity.getPayoutAmount());
            values.put(BRSQLiteHelper.BQGTX_PAYOUT_TXHASH, transactionEntity.getPayoutTxHash());

            database.beginTransaction();
            database.update(BRSQLiteHelper.BQGTX_TABLE_NAME, values, BRSQLiteHelper.BQGTX_COLUMN_ID + "=?",
                    new String[]{transactionEntity.getTxHash()});
            database.setTransactionSuccessful();
            return transactionEntity;
        } catch (Exception ex) {
            BRReportsManager.reportBug(ex);
            Log.e(TAG, "Error updating quick game tx into SQLite", ex);
            //Error in between database transaction
        } finally {
            database.endTransaction();
            closeDatabase();
            if (cursor != null) cursor.close();
        }
        return null;


    }

    public void deleteAllTransactions(Context app) {
        try {
            database = openDatabase();
            database.delete(BRSQLiteHelper.BQGTX_TABLE_NAME, null, null);
        } finally {
            closeDatabase();
        }
    }

    public List<DiceUiHolder> getAllTransactions(Context app) {
        List<DiceUiHolder> transactions = new ArrayList<>();
        Cursor cursor = null;
        try {
            database = openDatabase();

            cursor = database.query(BRSQLiteHelper.BQGTX_TABLE_NAME,
                    allColumns, null, null, null, null, null);

            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                DiceUiHolder transactionEntity = cursorToTransaction(app, cursor);
                transactions.add(transactionEntity);
                cursor.moveToNext();
            }

        } finally {
            closeDatabase();
            if (cursor != null)
                cursor.close();
            printTest(app);
        }
        return transactions;
    }


    public static DiceUiHolder cursorToTransaction(Context app, Cursor cursor) {

        return new DiceUiHolder(cursor.getString(0), BetQuickGamesEntity.BetTxType.fromValue(cursor.getInt(1)), cursor.getLong(2),
                    BetQuickGamesEntity.BetQuickGameType.fromValue(cursor.getInt(3)), BetQuickGamesEntity.BetDiceGameType.fromValue(cursor.getInt(4)), cursor.getLong(5),
                    cursor.getLong(8), cursor.getLong(6), cursor.getLong(7), cursor.getInt(9), cursor.getInt(10), cursor.getInt(11),
                    cursor.getLong(12), cursor.getString(13));
    }

    public void deleteTxByHash(Context app, String iso, String hash) {
        try {
            database = openDatabase();
            Log.e(TAG, "transaction deleted with id: " + hash);
            database.delete(BRSQLiteHelper.BQGTX_TABLE_NAME,
                    "_id=?", new String[]{hash, iso.toUpperCase()});
        } finally {
            closeDatabase();
        }
    }

    @Override
    public SQLiteDatabase openDatabase() {
        // Opening new database
        if (database == null || !database.isOpen())
            database = dbHelper.getWritableDatabase();
        dbHelper.setWriteAheadLoggingEnabled(BRConstants.WAL);
        return database;
    }

    @Override
    public void closeDatabase() {

    }

    private void printTest(Context app) {
        Cursor cursor = null;
        try {
            database = openDatabase();
            StringBuilder builder = new StringBuilder();

            cursor = database.query(BRSQLiteHelper.BQGTX_TABLE_NAME,
                    allColumns, null, null, null, null, null);
            builder.append("Total: " + cursor.getCount() + "\n");
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                BetQuickGamesEntity ent = cursorToTransaction(app, cursor);
                builder.append("ISO:" + ent.getTxISO() + ", Hash:" + ent.getTxHash() + ", blockHeight:" + ent.getBlockheight() + ", timeStamp:" + ent.getTimestamp()
                        + ", eventID:" + ent.getEventID() + ", amount:" + ent.getAmount()  + "\n");
            }
            Log.e(TAG, "printTest: " + builder.toString());
        } finally {
            if (cursor != null)
                cursor.close();
            closeDatabase();
        }
    }
}