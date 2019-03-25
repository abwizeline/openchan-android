package com.digiapp.openchan.viewMain.view;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.VpnService;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.digiapp.openchan.R;
import com.digiapp.openchan.models.circleButton.CuboidButton;
import com.digiapp.openchan.viewMain.presenters.MainActivityPresenter;
import com.hannesdorfmann.mosby3.mvp.MvpActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import cn.pedant.SweetAlert.SweetAlertDialog;
import de.blinkt.openvpn.api.IOpenVPNAPIService;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

import static com.digiapp.openchan.viewMain.presenters.MainActivityPresenter.ICS_OPENVPN_PERMISSION;

public class MainActivity extends MvpActivity<MainActivityPresenter.View, MainActivityPresenter> implements MainActivityPresenter.View, ServiceConnection {

    @BindView(R.id.balloon_2)
    ImageView balloon_2;
    @BindView(R.id.balloon_1)
    ImageView balloon_1;
    @BindView(R.id.balloon_3)
    ImageView balloon_3;
    @BindView(R.id.btnToken)
    Button btnCustomDNS;
    @BindView(R.id.btnCopyLogs)
    Button btnCopyLogs;
    @BindView(R.id.startStopVPN)
    CuboidButton startStopVPN;
    @BindView(R.id.vpnStatus)
    TextView vpnStatus;
    @BindView(R.id.btnDnsStatus)
    Button btnDnsStatus;
    @BindView(R.id.btnManageAccount)
    Button btnManageAccount;

    Unbinder unbinder;
    Handler mHandler = new Handler(Looper.getMainLooper());
    SweetAlertDialog mDialogProgress;
    public static int CODE_WRITE_SETTINGS_PERMISSION = 883;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        unbinder = ButterKnife.bind(this);
        startStopVPN.setCircle_hover_color(Color.LTGRAY);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @OnClick({R.id.btnToken, R.id.btnCopyLogs, R.id.startStopVPN, R.id.btnManageAccount, R.id.btnDnsStatus})
    public void onClick(View view) {
        int Id = view.getId();
        switch (Id) {
            case R.id.btnDnsStatus:
                LayoutInflater layoutInflater = getLayoutInflater();
                View customView = layoutInflater.inflate(R.layout.layout_changedns, null);
                EditText etPrimaryDns = customView.findViewById(R.id.etPrimaryDns);
                EditText etSecondaryDns = customView.findViewById(R.id.etSecondaryDns);
                etPrimaryDns.setText(getPresenter().getDns1());
                etSecondaryDns.setText(getPresenter().getDns2());

                SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE);
                sweetAlertDialog.setCustomView(customView);
                sweetAlertDialog.setTitleText("Set custom DNS settings");
                sweetAlertDialog.setTitleColor(getResources().getColor(R.color.color_bblue));
                sweetAlertDialog.setCancelText("Cancel");
                sweetAlertDialog.setConfirmText("OK");
                sweetAlertDialog.setConfirmClickListener(sweetAlertDialog1 -> {
                    if (youDesirePermissionCode(MainActivity.this)) {
                        android.provider.Settings.System.putString(getContentResolver(), android.provider.Settings.System.WIFI_STATIC_DNS1, etPrimaryDns.getText().toString());
                        android.provider.Settings.System.putString(getContentResolver(), android.provider.Settings.System.WIFI_STATIC_DNS2, etSecondaryDns.getText().toString());
                        setDNSEnabled(true);
                    } else {
                        openAndroidPermissionsMenu();
                    }
                    sweetAlertDialog1.dismiss();
                });
                sweetAlertDialog.show();
                break;
            case R.id.btnToken:
                askUserToken("ignore");
                break;
            case R.id.btnCopyLogs:
                getPresenter().copyLogs();

                SweetAlertDialog dialog = new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE);
                dialog.setTitleText("Logs copied to clipboard");
                dialog.setTitleColor(getResources().getColor(R.color.color_bblue));
                dialog.setConfirmText("Continue");
                dialog.show();
                break;

            case R.id.startStopVPN:
                getPresenter().startVPNClicked();
                break;

            case R.id.btnManageAccount:
                if (getPresenter().getToken() == null
                        || getPresenter().getToken().isEmpty()) {
                    View layout_input_token = getLayoutInflater().inflate(R.layout.layout_input_token, null);
                    EditText etToken = layout_input_token.findViewById(R.id.et_token);
                    etToken.setText(getPresenter().getToken() == null ? "" : getPresenter().getToken());

                    SweetAlertDialog sweetInputToken = getTokenInputDialog();
                    sweetInputToken.setCustomView(layout_input_token);
                    sweetInputToken.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            if (!etToken.getText().toString().trim().isEmpty()) {
                                getPresenter().checkToken(etToken.getText().toString().trim(), "manage_account");
                            }
                            sweetAlertDialog.dismissWithAnimation();
                        }
                    });
                    sweetInputToken.show();
                } else {
                    openAccount();
                }

                break;
        }
    }

    private void openAndroidPermissionsMenu() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, MainActivity.CODE_WRITE_SETTINGS_PERMISSION);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_SETTINGS}, MainActivity.CODE_WRITE_SETTINGS_PERMISSION);
        }
    }

    public static boolean youDesirePermissionCode(Activity context) {
        boolean permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permission = Settings.System.canWrite(context);
        } else {
            permission = ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_SETTINGS) == PackageManager.PERMISSION_GRANTED;
        }
        return permission;
    }

    @Override
    protected void onStart() {
        super.onStart();

        bindService();

        addBalloonAnimation(balloon_2, 1600);
        new Handler().postDelayed(() -> addBalloonAnimation(balloon_1, 1000), 0);
        new Handler().postDelayed(() -> addBalloonAnimation(balloon_3, 1200), 0);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService();
    }

    @NonNull
    @Override
    public MainActivityPresenter createPresenter() {
        return new MainActivityPresenter();
    }


    private void addBalloonAnimation(View view, int duration) {
        TranslateAnimation animate = new TranslateAnimation(
                TranslateAnimation.ABSOLUTE, 0f,
                TranslateAnimation.ABSOLUTE, 0f,
                view.getHeight() / 2, 60f,
                60, view.getHeight() / 2);
        animate.setDuration(duration);
        animate.setRepeatCount(-1);
        animate.setRepeatMode(Animation.REVERSE);
        animate.setFillAfter(true);
        view.startAnimation(animate);
    }

    private void unbindService() {
        unbindService(this);
        getPresenter().detachService();
    }

    private void bindService() {
        Intent vpnService = new Intent(IOpenVPNAPIService.class.getName());
        vpnService.setPackage("com.digiapp.openchan");

        boolean isBound = bindService(vpnService, this, Context.BIND_AUTO_CREATE);
        if (!isBound) {
            Toast.makeText(this, "isBound = " + isBound, Toast.LENGTH_SHORT).show();
        }
    }

    private void prepareStartProfile(int requestCode) throws RemoteException {
        Intent prepare = VpnService.prepare(this);
        if (prepare == null) {
            onActivityResult(requestCode, Activity.RESULT_OK, null);
        } else {
            startActivityForResult(prepare, requestCode);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        getPresenter().onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void showConnectedVpn() {
        mHandler.post(() -> {
            startStopVPN.setCircle_border_color(getResources().getColor(R.color.switch_on_color));
            startStopVPN.setCr_icon(R.drawable.switch_on);
            startStopVPN.setEnabled(true);
            startStopVPN.invalidate();
            vpnStatus.setText("Connected to VPN");
            vpnStatus.setTextColor(getResources().getColor(R.color.switch_on_color));

            setDNSEnabled(true);
        });
    }

    @Override
    public void showDisconnectedVpn() {
        mHandler.post(() -> {
            startStopVPN.setCircle_border_color(getResources().getColor(R.color.switch_off_color));
            startStopVPN.setCr_icon(R.drawable.switch_off);
            startStopVPN.setEnabled(true);
            startStopVPN.invalidate();
            vpnStatus.setText("Press to connect VPN");
            vpnStatus.setTextColor(getResources().getColor(R.color.switch_off_color));

            setDNSEnabled(false);
        });
    }

    @Override
    public void showProgressVPN() {
        mHandler.post(() -> {
            startStopVPN.setCircle_border_color(getResources().getColor(R.color.switch_conn_color));
            startStopVPN.setCr_icon(R.drawable.switch_progress);
            startStopVPN.setEnabled(false);
            startStopVPN.invalidate();
            vpnStatus.setText("Connecting to VPN");
            vpnStatus.setTextColor(getResources().getColor(R.color.switch_conn_color));
            btnDnsStatus.setTextColor(getResources().getColor(R.color.switch_conn_color));
            btnDnsStatus.setText("Connecting to Openchan DNS");
            btnDnsStatus.setEnabled(false);
            btnDnsStatus.setCompoundDrawablesWithIntrinsicBounds( null, null, null, null);
        });
    }

    private void setDNSEnabled(boolean enabled){
        if(enabled){
            btnDnsStatus.setTextColor(getResources().getColor(R.color.switch_on_color));
            btnDnsStatus.setText("Switched to Openchan DNS");

            Drawable img = MainActivity.this.getResources().getDrawable( R.drawable.check_icon );
            btnDnsStatus.setCompoundDrawablesWithIntrinsicBounds( img, null, null, null);
            btnDnsStatus.setCompoundDrawablePadding((int) (-28 * getResources().getDisplayMetrics().density + 0.5f));
            btnDnsStatus.setEnabled(true);

            final float scale = getResources().getDisplayMetrics().density;
            int padding_in_px = (int) (23 * scale + 0.5f);
            btnDnsStatus.setPadding(padding_in_px,btnDnsStatus.getPaddingTop(),btnDnsStatus.getPaddingRight(),btnDnsStatus.getPaddingBottom());
        }else{
            btnDnsStatus.setTextColor(getResources().getColor(R.color.switch_off_color));
            btnDnsStatus.setText("Switch to Openchan DNS");
            btnDnsStatus.setCompoundDrawablesWithIntrinsicBounds( null, null, null, null);
            btnDnsStatus.setPadding(0,btnDnsStatus.getPaddingTop(),btnDnsStatus.getPaddingRight(),btnDnsStatus.getPaddingBottom());
            btnDnsStatus.setEnabled(true);
        }
    }

    @Override
    public void showMessage(String message) {
        mHandler.post(() -> Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show());
    }

    private SweetAlertDialog getTokenInputDialog() {
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(MainActivity.this, SweetAlertDialog.NORMAL_TYPE);
        sweetAlertDialog.setTitleText("Type in your Token please");
        sweetAlertDialog.setTitleColor(getResources().getColor(R.color.color_bblue));
        sweetAlertDialog.setCancelText("Cancel");
        sweetAlertDialog.setConfirmText("OK");

        return sweetAlertDialog;
    }

    @Override
    public void askUserToken() {
        mHandler.post(() -> {
            askUserToken("vpn");
        });
    }

    private void askUserToken(String command) {
        LayoutInflater layoutInflater = getLayoutInflater();
        View customView = layoutInflater.inflate(R.layout.layout_input_token, null);
        EditText etToken = customView.findViewById(R.id.et_token);
        etToken.setText(getPresenter().getToken() == null ? "" : getPresenter().getToken());

        SweetAlertDialog sweetAlertDialog = getTokenInputDialog();
        sweetAlertDialog.setCustomView(customView);

        sweetAlertDialog.setConfirmClickListener(sweetAlertDialog1 -> {
            if (!etToken.getText().toString().trim().isEmpty()) {
                getPresenter().checkToken(etToken.getText().toString().trim(), command);
            }

            sweetAlertDialog.dismissWithAnimation();
        });
        sweetAlertDialog.show();
    }

    @Override
    public void requestVPN() {
        try {
            prepareStartProfile(ICS_OPENVPN_PERMISSION);
        } catch (RemoteException e) {
            e.printStackTrace();
            showMessage(e.toString());
        }
    }

    @Override
    public void showProgress(String message) {
        mHandler.post(() -> {
            // add progress here
        });
    }

    @Override
    public void dismissProgress() {
        mHandler.post(() -> {
            if (mDialogProgress != null) {
                mDialogProgress.dismissWithAnimation();
            }
        });
    }

    @Override
    public void tokenSuccess(String command) {
        SweetAlertDialog dialog = new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE);
        dialog.setTitleText("You're now logged in.");
        dialog.setTitleColor(getResources().getColor(R.color.color_bblue));
        dialog.setConfirmText("Continue");
        dialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                mHandler.post(() -> {
                    if (command.equalsIgnoreCase("vpn")) {
                        getPresenter().startVPNClicked();
                    } else if (command.equalsIgnoreCase("manage_account")) {
                        openAccount();
                    }
                });
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void openAccount() {
        String url = "https://openchan.com/auth/" + getPresenter().getToken();
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    @Override
    public void tokenFailed(String command) {
        mHandler.post(() -> {
            SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(MainActivity.this, SweetAlertDialog.ERROR_TYPE);
            sweetAlertDialog.setTitleText("Your token is invalid.");
            sweetAlertDialog.setTitleColor(getResources().getColor(R.color.color_bblue));
            sweetAlertDialog.setCancelText("Cancel");
            sweetAlertDialog.setConfirmText("Try Again");
            sweetAlertDialog.setConfirmClickListener(sweetAlertDialog1 -> {
                askUserToken(command);
                sweetAlertDialog.dismiss();
            });
            sweetAlertDialog.show();
        });
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        getPresenter().attachService(service);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        getPresenter().detachService();
    }
}
