package org.joinmastodon.android.fragments;

import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import org.joinmastodon.android.R;
import org.joinmastodon.android.ui.SimpleViewHolder;
import org.joinmastodon.android.ui.tabs.TabLayout;
import org.joinmastodon.android.ui.tabs.TabLayoutMediator;
import org.joinmastodon.android.ui.utils.UiUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import me.grishka.appkit.fragments.AppKitFragment;
import me.grishka.appkit.fragments.BaseRecyclerFragment;
import me.grishka.appkit.utils.V;

public class DiscoverFragment extends AppKitFragment implements ScrollableToTop{

	private TabLayout tabLayout;
	private ViewPager2 pager;
	private FrameLayout[] tabViews;
	private TabLayoutMediator tabLayoutMediator;

	private DiscoverPostsFragment postsFragment;
	private TrendingHashtagsFragment hashtagsFragment;
	private DiscoverNewsFragment newsFragment;
	private DiscoverAccountsFragment accountsFragment;

	private String accountID;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N)
			setRetainInstance(true);

		accountID=getArguments().getString("account");
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState){
		LinearLayout view=(LinearLayout) inflater.inflate(R.layout.fragment_discover, container, false);

		tabLayout=view.findViewById(R.id.tabbar);
		pager=view.findViewById(R.id.pager);

		tabViews=new FrameLayout[4];
		for(int i=0;i<tabViews.length;i++){
			FrameLayout tabView=new FrameLayout(getActivity());
			tabView.setId(switch(i){
				case 0 -> R.id.discover_posts;
				case 1 -> R.id.discover_hashtags;
				case 2 -> R.id.discover_news;
				case 3 -> R.id.discover_users;
				default -> throw new IllegalStateException("Unexpected value: "+i);
			});
			tabView.setVisibility(View.GONE);
			view.addView(tabView); // needed so the fragment manager will have somewhere to restore the tab fragment
			tabViews[i]=tabView;
		}

		tabLayout.setTabTextSize(V.dp(16));
		tabLayout.setTabTextColors(UiUtils.getThemeColor(getActivity(), R.attr.colorTabInactive), UiUtils.getThemeColor(getActivity(), android.R.attr.textColorPrimary));

		pager.setOffscreenPageLimit(4);
		pager.setAdapter(new DiscoverPagerAdapter());
		pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback(){
			@Override
			public void onPageSelected(int position){
				if(position==0)
					return;
				Fragment _page=getFragmentForPage(position);
				if(_page instanceof BaseRecyclerFragment){
					BaseRecyclerFragment page=(BaseRecyclerFragment) _page;
					if(!page.loaded && !page.isDataLoading())
						page.loadData();
				}
			}
		});

		if(postsFragment==null){
			Bundle args=new Bundle();
			args.putString("account", accountID);
			args.putBoolean("__is_tab", true);

			postsFragment=new DiscoverPostsFragment();
			postsFragment.setArguments(args);

			hashtagsFragment=new TrendingHashtagsFragment();
			hashtagsFragment.setArguments(args);

			newsFragment=new DiscoverNewsFragment();
			newsFragment.setArguments(args);

			accountsFragment=new DiscoverAccountsFragment();
			accountsFragment.setArguments(args);

			getChildFragmentManager().beginTransaction()
					.add(R.id.discover_posts, postsFragment)
					.add(R.id.discover_hashtags, hashtagsFragment)
					.add(R.id.discover_news, newsFragment)
					.add(R.id.discover_users, accountsFragment)
					.commit();
		}

		tabLayoutMediator=new TabLayoutMediator(tabLayout, pager, new TabLayoutMediator.TabConfigurationStrategy(){
			@Override
			public void onConfigureTab(@NonNull TabLayout.Tab tab, int position){
				tab.setText(switch(position){
					case 0 -> R.string.posts;
					case 1 -> R.string.hashtags;
					case 2 -> R.string.news;
					case 3 -> R.string.for_you;
					default -> throw new IllegalStateException("Unexpected value: "+position);
				});
				tab.view.textView.setAllCaps(true);
			}
		});
		tabLayoutMediator.attach();

		return view;
	}

	@Override
	public void scrollToTop(){

	}

	public void loadData(){
		if(postsFragment!=null && !postsFragment.loaded && !postsFragment.dataLoading)
			postsFragment.loadData();
	}

	private Fragment getFragmentForPage(int page){
		return switch(page){
			case 0 -> postsFragment;
			case 1 -> hashtagsFragment;
			case 2 -> newsFragment;
			case 3 -> accountsFragment;
			default -> throw new IllegalStateException("Unexpected value: "+page);
		};
	}

	private class DiscoverPagerAdapter extends RecyclerView.Adapter<SimpleViewHolder>{
		@NonNull
		@Override
		public SimpleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
			FrameLayout view=tabViews[viewType];
			((ViewGroup)view.getParent()).removeView(view);
			view.setVisibility(View.VISIBLE);
			view.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
			return new SimpleViewHolder(view);
		}

		@Override
		public void onBindViewHolder(@NonNull SimpleViewHolder holder, int position){}

		@Override
		public int getItemCount(){
			return 4;
		}

		@Override
		public int getItemViewType(int position){
			return position;
		}
	}
}