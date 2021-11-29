package com.chwishay.orthrecnursing

import androidx.appcompat.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable

//                       _ooOoo_
//                      o8888888o
//                      88" . "88
//                      (| -_- |)
//                       O\ = /O
//                   ____/`---'\____
//                 .   ' \\| |// `.
//                  / \\||| : |||// \
//                / _||||| -:- |||||- \
//                  | | \\\ - /// | |
//                | \_| ''\---/'' | |
//                 \ .-\__ `-` ___/-. /
//              ______`. .' /--.--\ `. . __
//           ."" '< `.___\_<|>_/___.' >'"".
//          | | : `- \`.;`\ _ /`;.`/ - ` : | |
//            \ \ `-. \_ __\ /__ _/ .-` / /
//    ======`-.____`-.___\_____/___.-`____.-'======
//                       `=---='
//
//    .............................................
//             佛祖保佑             永无BUG
/**
 * author:RanQing
 * date:2021/10/28 0028 2:36
 * description:
 */
abstract class BaseActivity: AppCompatActivity() {
    var defaultCompositeDisposable = CompositeDisposable()
    override fun onDestroy() {
        defaultCompositeDisposable.clear()
        super.onDestroy()
    }
}