package tfsapps.lovepriceplus;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.android.billingclient.api.*;
import android.content.Intent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SubscriptionActivity extends AppCompatActivity implements PurchasesUpdatedListener{

    private BillingClient billingClient;
    private static final String TAG = "tag-ad-free-SubscriptionActivity";
    private static final String SUBSCRIPTION_SKU = "ad_free_plan"; //
    private Button purchaseButton;
    private Boolean isPurchased = false;    //購入フラグ

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ad_free_subscription);

        purchaseButton = findViewById(R.id.purchaseButton);
        purchaseButton.setEnabled(false);  // ボタンは無効
        purchaseButton.setBackgroundColor(Color.rgb(200, 200, 200));
        Button backButton = findViewById(R.id.btnBack);

        // BillingClientの初期化
        billingClient = BillingClient.newBuilder(this)
                .enablePendingPurchases()
                .setListener(this)
                .build();

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    queryProductDetails();
                    runOnUiThread(() -> {
                        purchaseButton.setEnabled(true);  // ボタンは有効
                        purchaseButton.setBackgroundColor(Color.argb(240, 0, 85, 77));
                        purchaseButton.invalidate();
                    });
                    Log.e(TAG, "Billing Client is ready ");
                } else {
                    Log.e(TAG, "Billing setup failed: " + billingResult.getDebugMessage());
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                Log.e(TAG, "Billing service disconnected");
                purchaseButton.setEnabled(false);  // ボタンは無効
                purchaseButton.setBackgroundColor(Color.argb(150, 169, 169, 169));
            }
        });

        // 「購入する」ボタンのクリックリスナー
        purchaseButton.setOnClickListener(v -> initialPurchase());

        // 「戻る」ボタンのクリックリスナー
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(SubscriptionActivity.this, MainActivity.class);
            intent.putExtra("PURCHASE_FLAG", isPurchased); // フラグを追加
            startActivity(intent);
            finish();
        });
    }

    private void queryProductDetails(){
        QueryProductDetailsParams.Product product = QueryProductDetailsParams.Product.newBuilder()
                .setProductId(SUBSCRIPTION_SKU)
                .setProductType(BillingClient.ProductType.SUBS)
                .build();

        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                .setProductList(List.of(product))
                .build();

        billingClient.queryProductDetailsAsync(params, (billingResult, productDetailsList) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && productDetailsList != null) {
                if (!productDetailsList.isEmpty()) {
                    Log.e(TAG, "successful!!");
                } else {
                    Log.e(TAG, "empty!!");
                }
            } else {
                Log.e(TAG, "Failed to ...." + billingResult.getDebugMessage());
            }
        });
    }

    // SKU詳細を取得するメソッド
    private void initialPurchase() {
        QueryProductDetailsParams.Product product = QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(SUBSCRIPTION_SKU)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build();

        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                .setProductList(List.of(product))
                .build();

        billingClient.queryProductDetailsAsync(params, (billingResult, productDetailsList) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && productDetailsList != null){
                for (ProductDetails productDetails : productDetailsList){
                    if (SUBSCRIPTION_SKU.equals(productDetails.getProductId())){
                        try {
                            String price = productDetails.getSubscriptionOfferDetails().get(0).getPricingPhases().getPricingPhaseList().get(0).getFormattedPrice();
                            String biilingPeriod = productDetails.getSubscriptionOfferDetails().get(0).getPricingPhases().getPricingPhaseList().get(0).getBillingPeriod();
                            String offerToken = productDetails.getSubscriptionOfferDetails().get(0).getOfferToken();

                            Log.d(TAG, ">>>> Pri="+price+" Period="+biilingPeriod);

                            BillingFlowParams.ProductDetailsParams productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
                                    .setProductDetails(productDetails)
                                    .setOfferToken(offerToken)
                                            .build();

                            BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                                    .setProductDetailsParamsList(List.of(productDetailsParams))
                                            .build();

                            //デバッグログ
                            Log.d(TAG, "Launching billing flow....");

                            runOnUiThread(() -> {
                                BillingResult result = billingClient.launchBillingFlow(SubscriptionActivity.this, billingFlowParams);
                                if (result.getResponseCode() != BillingClient.BillingResponseCode.OK) {
                                    Log.d(TAG, "Failed to launch billing flow...." + result.getDebugMessage());
                                }
                            });
                        } catch (Exception e){
                            Log.e(TAG, "Error in building billing flow params:"+e.getMessage(),e);
                        }
                    }
                }
            }
        });

    }

    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, List<Purchase>purchases){
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null){
            for (Purchase purchase : purchases){
                if (SUBSCRIPTION_SKU.equals(purchase.getProducts().get(0))){
                    //購入が成功した場合の処理
                    //handle the purchase
                    handlePurchase(purchase);
                }
            }
        }
        else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED){
            //handle user cancellation
            Toast.makeText(this, "キャセルしました。ご購入再検討下さい。", Toast.LENGTH_LONG).show();
        }
        else{
            //handle other errors
            //Toast.makeText(this, "エラー", Toast.LENGTH_LONG).show();
        }
    }

    private void handlePurchase(Purchase purchase){
        //購入の検証やサーバーへの通知など、必要な処理を行う
        Toast.makeText(this, "購入ありがとうございます。", Toast.LENGTH_LONG).show();
//        Toast.makeText(this, "Purchase successful!!"+purchase.getOrderId(), Toast.LENGTH_LONG).show();
        Log.e(TAG, "Purchase successful!!"+purchase.getOrderId());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (billingClient != null) {
            billingClient.endConnection();
        }
    }
}
