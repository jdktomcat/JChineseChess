package com.jdktomcat.chinese.chess.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.SnackbarUtils;
import com.blankj.utilcode.util.StringUtils;
import com.jdktomcat.chinese.chess.R;
import com.jdktomcat.chinese.chess.game.GameConfig;
import com.jdktomcat.chinese.chess.game.GameLogic;
import com.jdktomcat.chinese.chess.game.IGameCallback;
import com.jdktomcat.chinese.chess.view.GameBoardView;

import java.util.LinkedList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 主活动区
 *
 * @author jdktomcat
 * @date 2020-03-18
 */
public class MainActivity extends AppCompatActivity implements IGameCallback {

    /**
     * 棋盘
     */
    @BindView(R.id.game_board)
    GameBoardView mGameBoard;

    /**
     * 游戏进度条
     */
    @BindView(R.id.game_progress)
    ProgressBar mGameProgress;

    /**
     * 声音池
     */
    private SoundPool mSoundPool;

    /**
     * 音频列表
     */
    private LinkedList<Integer> mSoundList;

    /**
     * 游戏逻辑
     */
    private GameLogic mGameLogic;

    /**
     * 共享属性
     */
    private SharedPreferences mPreference;

    /**
     * 是否开启声音
     */
    private boolean mSoundEnable;

    /**
     * 障碍索引
     */
    private int mHandicapIndex;

    /**
     * 电脑
     */
    private boolean mComputerFlip;

    /**
     * 棋子样式
     */
    private int mPieceStyle;

    /**
     * 电脑棋力
     */
    private int mAILevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mPreference = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        loadDefaultConfig();
        initSoundPool();
        initGameLogic();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.main_menu_exit:
                finish();
                break;
            case R.id.main_menu_retract:
                mGameLogic.retract();
                break;
            case R.id.main_menu_restart:
                mGameLogic.restart(mComputerFlip, mHandicapIndex);
                showMessage(getString(R.string.new_game_started));
                break;
            case R.id.main_menu_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.main_menu_about:
                startActivity(new Intent(this, AboutActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDefaultConfig();
        mGameLogic.setLevel(mAILevel);
        mGameBoard.setPieceTheme(mPieceStyle);
        mGameBoard.invalidate();
    }

    @Override
    protected void onDestroy() {
        if (mSoundPool != null) {
            mSoundPool.release();
        }
        mPreference.edit().putString(GameConfig.PREF_LAST_FEN, mGameLogic.getCurrentFen()).apply();
        super.onDestroy();
    }

    private void loadDefaultConfig() {
        mSoundEnable = mPreference.getBoolean(getString(R.string.pref_sound_key), true);
        mHandicapIndex = Integer.parseInt(mPreference.getString(getString(R.string.pref_handicap_key), "0"));
        mComputerFlip = mPreference.getBoolean(getString(R.string.pref_who_first_key), false);
        mPieceStyle = Integer.parseInt(mPreference.getString(getString(R.string.pref_piece_style_key), "0"));
        mAILevel = Integer.parseInt(mPreference.getString(getString(R.string.pref_level_key), "0"));
    }

    private void initSoundPool() {
        mSoundList = new LinkedList<>();
        int poolSize = GameConfig.SOUND_RES_ARRAY.length;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mSoundPool = new SoundPool.Builder().setMaxStreams(poolSize).build();
        } else {
            mSoundPool = new SoundPool(poolSize, AudioManager.STREAM_MUSIC, 0);
        }
        for (int res : GameConfig.SOUND_RES_ARRAY) {
            mSoundList.add(mSoundPool.load(this, res, 1));
        }
    }

    private void initGameLogic() {
        mGameLogic = mGameBoard.getGameLogic();
        mGameLogic.setCallback(this);
        mGameLogic.setLevel(mAILevel);
        mGameBoard.setPieceTheme(mPieceStyle);
        // load last saved game
        String lastFen = mPreference.getString(GameConfig.PREF_LAST_FEN, "");
        if (StringUtils.isEmpty(lastFen)) {
            mGameLogic.restart(mComputerFlip, mHandicapIndex);
        } else {
            showMessage(getString(R.string.load_last_game_finish));
            mGameLogic.restart(mComputerFlip, lastFen);
        }
    }

    @Override
    public void postPlaySound(final int soundIndex) {
        if (mSoundPool != null && mSoundEnable) {
            int soundId = mSoundList.get(soundIndex);
            mSoundPool.play(soundId, 1, 1, 0, 0, 1);
        }
    }

    @Override
    public void postShowMessage(final String message) {
        runOnUiThread(() -> showMessage(message));
    }

    private void showMessage(String message) {
        SnackbarUtils.with(mGameBoard).setDuration(SnackbarUtils.LENGTH_LONG).setMessage(message).show();
    }

    @Override
    public void postShowMessage(int messageId) {
        postShowMessage(getString(messageId));
    }

    @Override
    public void postStartThink() {
        runOnUiThread(() -> mGameProgress.setVisibility(View.VISIBLE));
    }

    @Override
    public void postEndThink() {
        runOnUiThread(() -> mGameProgress.setVisibility(View.GONE));
    }
}
