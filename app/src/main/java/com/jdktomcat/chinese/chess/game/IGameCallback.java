package com.jdktomcat.chinese.chess.game;

/**
 * 游戏回调接口
 *
 * @author jdktomcat
 * @date 2018/3/9.
 */

public interface IGameCallback {

    /**
     * 音响
     *
     * @param soundIndex 声音标识
     */
    void postPlaySound(int soundIndex);

    /**
     * 显示消息
     *
     * @param message 消息
     */
    void postShowMessage(String message);

    /**
     * 显示消息
     *
     * @param messageId 消息标识
     */
    void postShowMessage(int messageId);

    /**
     * 开始思考
     */
    void postStartThink();

    /**
     * 结束思考
     */
    void postEndThink();
}
