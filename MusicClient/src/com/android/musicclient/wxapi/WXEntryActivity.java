package com.android.musicclient.wxapi;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler{

    private IWXAPI api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        api= WXAPIFactory.createWXAPI(this,"wx2c556b157cf5424d",false);
        api.handleIntent(getIntent(),this);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onReq(BaseReq baseReq) {

    }

    @Override
    public void onResp(BaseResp Resp) {
        switch (Resp.errCode) {
            case BaseResp.ErrCode.ERR_OK:
                //分享成功
                Toast.makeText(this,"成功分享到朋友圈",Toast.LENGTH_SHORT).show();
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                //分享取消
                Toast.makeText(this,"取消分享",Toast.LENGTH_SHORT).show();
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                //分享拒绝
                Toast.makeText(this,"分享拒绝",Toast.LENGTH_SHORT).show();
            case BaseResp.ErrCode.ERR_SENT_FAILED:
                //分享失败
                Toast.makeText(this,"分享失败",Toast.LENGTH_SHORT).show();
        }
    }
}
