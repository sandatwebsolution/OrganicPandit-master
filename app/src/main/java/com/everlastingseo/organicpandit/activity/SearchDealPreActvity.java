package com.everlastingseo.organicpandit.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.everlastingseo.organicpandit.R;
import com.everlastingseo.organicpandit.adapter.SearchPostReqirmentAdapter;
import com.everlastingseo.organicpandit.helper.ApiClient;
import com.everlastingseo.organicpandit.helper.ApiService;
import com.everlastingseo.organicpandit.helper.ApplicationConstatnt;
import com.everlastingseo.organicpandit.helper.CustomProgressDialog;
import com.everlastingseo.organicpandit.pagination.PaginationAdapterCallback;
import com.everlastingseo.organicpandit.pagination.PaginationScrollListener;
import com.everlastingseo.organicpandit.pojo.citylist.CityData;
import com.everlastingseo.organicpandit.pojo.citylist.CityRespose;
import com.everlastingseo.organicpandit.pojo.postrequirment.PostReqirmentResponse;
import com.everlastingseo.organicpandit.pojo.postrequirment.PostRequirmentData;
import com.everlastingseo.organicpandit.pojo.statelist.StateData;
import com.everlastingseo.organicpandit.pojo.statelist.StateResponse;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchDealPreActvity extends AppCompatActivity implements PaginationAdapterCallback {


    private static final String TAG = "MainActivity";
    private static final int PAGE_START = 1;
    SearchPostReqirmentAdapter adapter;
    LinearLayoutManager linearLayoutManager;
    RecyclerView rv;
    ProgressBar progressBar;
    LinearLayout errorLayout;
    Button btnRetry;
    TextView txtError;
    SwipeRefreshLayout swipeRefreshLayout;
    ApiService apiService;
    Context mContext;
    List<StateData> stateDataList = new ArrayList<>();
    List<CityData> cityDataList = new ArrayList<>();
    String StateId = "";
    String CityId = "";
    AdapterView.OnItemSelectedListener listenerCity = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            if (cityDataList.size() > 0) {
                CityId = cityDataList.get(position).getId();
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };
    ProgressDialog progressDialog;
    private int TOTAL_PAGES = 1;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private int currentPage = PAGE_START;
    // limiting to 5 for this tutorial, since total pages in actual API is very large. Feel free to modify.
    private SearchableSpinner mSpinnerSelectState;
    private SearchableSpinner mSpinnerSelectCity;
    AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            if (stateDataList.size() > 0) {
                callCityList(stateDataList.get(position).getId());
                StateId = stateDataList.get(position).getId();

            } else {

            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };
    private com.everlastingseo.organicpandit.utils.font.CustomBoldButton mBtnSerchpost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pagination);
        bindview();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void bindview() {
        mContext = SearchDealPreActvity.this;
        apiService = ApiClient.getClient(getApplicationContext())
                .create(ApiService.class);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setTitle("Search Post");

        progressDialog = CustomProgressDialog.ctor(SearchDealPreActvity.this);

                rv = findViewById(R.id.main_recycler);
        progressBar = findViewById(R.id.main_progress);
        errorLayout = findViewById(R.id.error_layout);
        btnRetry = findViewById(R.id.error_btn_retry);
        txtError = findViewById(R.id.error_txt_cause);
        swipeRefreshLayout = findViewById(R.id.main_swiperefresh);

        adapter = new SearchPostReqirmentAdapter(this);

        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rv.setLayoutManager(linearLayoutManager);
        rv.setItemAnimator(new DefaultItemAnimator());

        rv.setAdapter(adapter);


        rv.addOnScrollListener(new PaginationScrollListener(linearLayoutManager) {
            @Override
            protected void loadMoreItems() {
                isLoading = true;
                currentPage += 1;

                loadNextPage();
            }

            @Override
            public int getTotalPageCount() {
                return TOTAL_PAGES;
            }

            @Override
            public boolean isLastPage() {
                return isLastPage;
            }

            @Override
            public boolean isLoading() {
                return isLoading;
            }
        });


        btnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                loadFirstPage();
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                doRefresh();
            }
        });

        mSpinnerSelectState = (SearchableSpinner) findViewById(R.id.SpinnerSelectState);
        mSpinnerSelectCity = (SearchableSpinner) findViewById(R.id.SpinnerSelectCity);
        mBtnSerchpost = (com.everlastingseo.organicpandit.utils.font.CustomBoldButton) findViewById(R.id.BtnSerchpost);

        callStateList("101");
        mBtnSerchpost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (StateId == null) {
                    ApplicationConstatnt.getDialog(mContext, "", "Select State");
                } else {

                    doRefresh();

                }

            }
        });
        mSpinnerSelectState.setOnItemSelectedListener(listener);
        mSpinnerSelectCity.setOnItemSelectedListener(listenerCity);
        doRefresh();
    }

    private void doRefresh() {
        progressBar.setVisibility(View.VISIBLE);
        if (callTopRatedMoviesApi().isExecuted())
            callTopRatedMoviesApi().cancel();

        // TODO: Check if data is stale.
        //  Execute network request if cache is expired; otherwise do not update data.
        currentPage = PAGE_START;
        isLastPage = false;
        adapter.clear();
        adapter.notifyDataSetChanged();
        loadFirstPage();
        swipeRefreshLayout.setRefreshing(false);
    }

    private void loadNextPage() {
        Log.d(TAG, "loadNextPage: " + currentPage);

        callTopRatedMoviesApi().enqueue(new Callback<PostReqirmentResponse>() {
            @Override
            public void onResponse(Call<PostReqirmentResponse> call, Response<PostReqirmentResponse> response) {
                try {
                    if (response.body().getSuccess()) {
                        adapter.removeLoadingFooter();
                        isLoading = false;

                        List<PostRequirmentData> results = fetchResults(response);
                        adapter.addAll(results);

                        if (currentPage != TOTAL_PAGES) adapter.addLoadingFooter();
                        else isLastPage = true;
                    } else {
                        adapter.removeLoadingFooter();
                        Toast.makeText(mContext, "DataData Not Found", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(mContext, "Data Not Found", Toast.LENGTH_LONG).show();
                }

            }

            @Override
            public void onFailure(Call<PostReqirmentResponse> call, Throwable t) {
                t.printStackTrace();
                adapter.showRetry(true, fetchErrorMessage(t));
            }
        });
    }

    private List<PostRequirmentData> fetchResults(Response<PostReqirmentResponse> response) {
        PostReqirmentResponse topRatedMovies = response.body();
        return topRatedMovies.getData();
    }

    private void loadFirstPage() {
        Log.d(TAG, "loadFirstPage: ");

        if(progressDialog.isShowing()){
            progressBar.setVisibility(View.GONE);
        }else
            progressBar.setVisibility(View.VISIBLE);

        hideErrorView();
        currentPage = PAGE_START;
        callTopRatedMoviesApi().enqueue(new Callback<PostReqirmentResponse>() {
            @Override
            public void onResponse(Call<PostReqirmentResponse> call, Response<PostReqirmentResponse> response) {
                hideErrorView();
                progressBar.setVisibility(View.GONE);

                try {
                    TOTAL_PAGES = Integer.parseInt(response.body().getTotal_page());
                    List<PostRequirmentData> results = fetchResults(response);
                    adapter.addAll(results);

                    if (currentPage <= TOTAL_PAGES) adapter.addLoadingFooter();
                    else isLastPage = true;
                } catch (Exception e) {
                    Toast.makeText(mContext, "Data Not Found", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<PostReqirmentResponse> call, Throwable t) {
                t.printStackTrace();
                showErrorView(t);
            }
        });
    }

    private Call<PostReqirmentResponse> callTopRatedMoviesApi() {
        return apiService.Testpostrequirement_list(
                StateId,
                CityId,
                String.valueOf(currentPage)
        );
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        doRefresh();
    }


    private void callCityList(String id) {
//        final ProgressDialog progressDialog = CustomProgressDialog.ctor(SearchDealPreActvity.this);
        progressDialog.show();

        apiService.getcities(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<CityRespose>() {
                    @Override
                    public void onSuccess(CityRespose userTypeResponse) {
                        progressDialog.dismiss();
                        if (userTypeResponse.getSuccess()) {
                            cityDataList.clear();
                            cityDataList = userTypeResponse.getData();
                            CityData userTypeDatatest = new CityData();
                            userTypeDatatest.setName("Select City");
                            cityDataList.add(0, userTypeDatatest);
                            ArrayAdapter<CityData> adapter = new ArrayAdapter<CityData>(mContext, R.layout.spinner_item_textcolorblck, cityDataList);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            mSpinnerSelectCity.setAdapter(adapter);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        progressDialog.dismiss();
                        Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void callStateList(String id) {
        progressDialog.show();

        apiService.getstateList(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<StateResponse>() {
                    @Override
                    public void onSuccess(StateResponse userTypeResponse) {
//                        progressDialog.dismiss();
                        if (userTypeResponse.getSuccess()) {
                            stateDataList.clear();
                            stateDataList = userTypeResponse.getData();
                            StateData userTypeDatatest = new StateData();
                            userTypeDatatest.setName("Select State");
                            stateDataList.add(0, userTypeDatatest);
                            ArrayAdapter<StateData> adapter = new ArrayAdapter<StateData>(mContext, R.layout.spinner_item_textcolorblck, stateDataList);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            mSpinnerSelectState.setAdapter(adapter);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        progressDialog.dismiss();
                        Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void hideErrorView() {
        if (errorLayout.getVisibility() == View.VISIBLE) {
            errorLayout.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Remember to add android.permission.ACCESS_NETWORK_STATE permission.
     *
     * @return
     */
    private String fetchErrorMessage(Throwable throwable) {
        String errorMsg = getResources().getString(R.string.error_msg_unknown);

        if (!isNetworkConnected()) {
            errorMsg = getResources().getString(R.string.error_msg_no_internet);
        } else if (throwable instanceof TimeoutException) {
            errorMsg = getResources().getString(R.string.error_msg_timeout);
        }

        return errorMsg;
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    private void showErrorView(Throwable throwable) {

        if (errorLayout.getVisibility() == View.GONE) {
            errorLayout.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);

            txtError.setText(fetchErrorMessage(throwable));
        }
    }

    @Override
    public void retryPageLoad() {
        loadNextPage();
    }
}
