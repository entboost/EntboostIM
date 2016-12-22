package com.entboost.im.contact;

import net.yunim.service.EntboostCache;
import net.yunim.service.EntboostUM;
import net.yunim.service.entity.CardInfo;
import net.yunim.service.entity.MemberInfo;
import net.yunim.service.listener.QueryUserListener;
import net.yunim.utils.YIResourceUtils;

import org.apache.commons.lang3.StringUtils;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.entboost.handler.HandlerToolKit;
import com.entboost.im.R;
import com.entboost.im.base.EbActivity;
import com.entboost.im.global.MyApplication;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.nostra13.universalimageloader.core.ImageLoader;

public class DefaultUserInfoActivity extends EbActivity {
	@ViewInject(R.id.user_head)
	private ImageView userHead;
	@ViewInject(R.id.na)
	private TextView na;
	@ViewInject(R.id.account)
	private TextView account;
	@ViewInject(R.id.uid)
	private TextView uid;
	@ViewInject(R.id.ph)
	private TextView ph;
	@ViewInject(R.id.tel)
	private TextView tel;
	@ViewInject(R.id.em)
	private TextView em;
	@ViewInject(R.id.ti)
	private TextView ti;
	@ViewInject(R.id.de)
	private TextView de;
	@ViewInject(R.id.en)
	private TextView en;
	@ViewInject(R.id.ad)
	private TextView ad;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAbContentView(R.layout.activity_default_user_info);
		
		ViewUtils.inject(this);
		final Long uid = getIntent().getLongExtra("uid", -1);
		if (uid > 0) {
			EntboostUM.loadDefaultCardInfo(uid + "", new QueryUserListener() {
				@Override
				public void onFailure(int code, String errMsg) {
					//refreshView(EntboostCache.getCardInfo(uid));
				}
				
				@Override
				public void onQueryUserSuccess(final CardInfo cardInfo) {
					refreshView(cardInfo);
				}
			});
		}
	}
	
	//刷新视图
	private void refreshView(final CardInfo card) {
		if (card != null) {
			HandlerToolKit.runOnMainThreadAsync(new Runnable() {
				@Override
				public void run() {
					
						if(StringUtils.isNotBlank(card.getEc())) {
							Long mid=Long.valueOf(card.getEc());
							MemberInfo member = EntboostCache.getMemberByCode(mid);
							if(member!=null){
								Bitmap img = YIResourceUtils.getHeadBitmap(member.getH_r_id());
								if (img != null) {
									userHead.setImageBitmap(img);
								} else {
									ImageLoader.getInstance().displayImage(member.getHeadUrl(), userHead,
											MyApplication.getInstance().getUserImgOptions());
								}
							}
						}
						
						if (card.getAccount()!=null)
							account.setText(card.getAccount());
						if (card.getTo_card()!=null)
							uid.setText(String.valueOf(card.getTo_card()));
						na.setText(card.getNa());
						ph.setText(card.getPh());
						tel.setText(card.getTel());
						em.setText(card.getEm());
						ti.setText(card.getTi());
						de.setText(card.getDe());
						en.setText(card.getEn());
						ad.setText(card.getAd());
					}
				
			});
		}
	}
}
