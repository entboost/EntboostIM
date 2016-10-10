package com.entboost.im.contact;

import net.yunim.service.EntboostCache;
import net.yunim.service.entity.CardInfo;
import net.yunim.service.entity.MemberInfo;
import net.yunim.utils.ResourceUtils;

import org.apache.commons.lang3.StringUtils;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.entboost.im.R;
import com.entboost.im.base.EbActivity;
import com.entboost.im.global.MyApplication;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.nostra13.universalimageloader.core.ImageLoader;

public class DefaultUserInfoActivity extends EbActivity {
	@ViewInject(R.id.user_head)
	private ImageView userHead;
	@ViewInject(R.id.na)
	private TextView na;
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
		Long uid = getIntent().getLongExtra("uid", -1);
		if (uid > 0) {
			CardInfo card = EntboostCache.getCardInfo(uid);
			if (card != null) {
				if(StringUtils.isNotBlank(card.getEc())){
					Long mid=Long.valueOf(card.getEc());
					MemberInfo member = EntboostCache.getMemberByCode(mid);
					if(member!=null){
						Bitmap img = ResourceUtils.getHeadBitmap(member.getH_r_id());
						if (img != null) {
							userHead.setImageBitmap(img);
						} else {
							ImageLoader.getInstance().displayImage(member.getHeadUrl(),
									userHead,
									MyApplication.getInstance().getImgOptions());
						}
					}
				}
				na.setText(card.getNa());
				ph.setText(card.getPh());
				tel.setText(card.getTel());
				em.setText(card.getEm());
				ti.setText(card.getTi());
				de.setText(card.getDe());
				en.setText(card.getEn());
				ad.setText(card.getAd());
			}
		}
	}

}
