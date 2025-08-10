package sar.id.mlt.dr;

import android.Manifest;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import sar.id.mlt.dr.databinding.MainBinding;

public class MainActivity extends AppCompatActivity {

	private MainBinding binding;
	private ValueCallback<Uri[]> filePathCallback;
	private final static int PERMISSION_REQUEST_CODE = 1001;
	private final String[] REQUIRED_PERMISSIONS =
			Build.VERSION.SDK_INT <= Build.VERSION_CODES.P
					? new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}
					: new String[]{Manifest.permission.CAMERA};
	private final ActivityResultLauncher<Intent> fileChooserLauncher =
			registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
				if (filePathCallback != null) {
					Uri[] results = WebChromeClient.FileChooserParams.parseResult(result.getResultCode(), result.getData());
					filePathCallback.onReceiveValue(results);
					filePathCallback = null;
				}
			});
	private final ActivityResultLauncher<String[]> permissionLauncher =
			registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), map -> {
				if (allPermissionsGranted()) {
					initializeApp();
				} else {
					Toast.makeText(this, "Camera permission required for full functionality.", Toast.LENGTH_LONG).show();
					initializeApp(); // still load but with reduced features
				}
			});
	private long backPressedTime = 0L;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = MainBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		binding.webView.setVisibility(View.GONE);

		if (allPermissionsGranted()) {
			initializeApp();
		} else {
			permissionLauncher.launch(REQUIRED_PERMISSIONS);
		}
	}

	private void initializeApp() {
		binding.webView.setVisibility(View.VISIBLE);
		WebSettings webSettings = binding.webView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webSettings.setDomStorageEnabled(true);
		webSettings.setDatabaseEnabled(true);
		webSettings.setAllowFileAccess(true);
		webSettings.setAllowContentAccess(true);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			try {
				webSettings.setAllowFileAccessFromFileURLs(false);
				webSettings.setAllowUniversalAccessFromFileURLs(false);
			} catch (Exception ignored) { }
		}

		webSettings.setJavaScriptCanOpenWindowsAutomatically(false);
		webSettings.setMediaPlaybackRequiresUserGesture(false);
		webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			try {
				webSettings.setSafeBrowsingEnabled(true);
			} catch (Exception ignored) { }
		}

		binding.webView.addJavascriptInterface(new WebAppInterface(this), "Android");

		binding.webView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
				Uri uri = request.getUrl();
				String scheme = uri.getScheme();
				if ("file".equals(scheme)) {
					return false;
				}
				String host = uri.getHost();
				if (host != null && (host.equals("example.com") || host.endsWith(".example.com"))) {
					return false;
				}
				Intent i = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(i);
				return true;
			}
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
			}
			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
			}
		});

		binding.webView.setWebChromeClient(new WebChromeClient() {
			@Override
			public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback,
											 FileChooserParams fileChooserParams) {
				if (MainActivity.this.filePathCallback != null) {
					MainActivity.this.filePathCallback.onReceiveValue(null);
				}
				MainActivity.this.filePathCallback = filePathCallback;

				try {
					String[] acceptTypes = fileChooserParams.getAcceptTypes();
					boolean isCsvRequest = false;
					if (acceptTypes != null) {
						for (String type : acceptTypes) {
							if (type != null && type.toLowerCase().contains("csv")) {
								isCsvRequest = true;
								break;
							}
						}
					}

					Intent intent = fileChooserParams.createIntent();
					if (isCsvRequest) {
						intent.setType("*/*");
						String[] mimeTypes = {"text/csv", "text/comma-separated-values"};
						intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
					}

					fileChooserLauncher.launch(intent);
				} catch (ActivityNotFoundException e) {
					Toast.makeText(MainActivity.this, "No file chooser found", Toast.LENGTH_SHORT).show();
					MainActivity.this.filePathCallback = null;
					return false;
				}
				return true;
			}
			@Override
			public void onPermissionRequest(final PermissionRequest request) {
				for (String r : request.getResources()) {
					if (PermissionRequest.RESOURCE_VIDEO_CAPTURE.equals(r)) {
						request.grant(new String[]{PermissionRequest.RESOURCE_VIDEO_CAPTURE});
						break;
					}
				}
			}
		});

		binding.webView.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
			try {
				DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
				request.setMimeType(mimetype);
				request.addRequestHeader("User-Agent", userAgent);
				request.setDescription("Downloading file...");
				String fileName = URLUtil.guessFileName(url, contentDisposition, mimetype);
				request.setTitle(fileName);
				request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

				request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

				DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
				if (dm != null) {
					dm.enqueue(request);
					Toast.makeText(getApplicationContext(), "Downloading File...", Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(getApplicationContext(), "Download manager unavailable", Toast.LENGTH_LONG).show();
				}
			} catch (Exception e) {
				Toast.makeText(getApplicationContext(), "Download Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
			}
		});

		binding.webView.loadUrl("file:///android_asset/index.html");

		OnBackPressedCallback backCallback = new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				if (binding.webView.canGoBack()) {
					binding.webView.goBack();
				} else {
					long now = System.currentTimeMillis();
					if (backPressedTime + 2000 > now) {
						setEnabled(false);
						getOnBackPressedDispatcher().onBackPressed();
					} else {
						backPressedTime = now;
						Toast.makeText(MainActivity.this, "Press again to exit", Toast.LENGTH_SHORT).show();
					}
				}
			}
		};
		getOnBackPressedDispatcher().addCallback(this, backCallback);
		if (Build.VERSION.SDK_INT >= 33) {
			try {
				getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
						android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT,
						() -> {
							runOnUiThread(() -> {
								if (binding.webView.canGoBack()) {
									binding.webView.goBack();
								} else {
									long now = System.currentTimeMillis();
									if (backPressedTime + 2000 > now) {
										finish();
									} else {
										backPressedTime = now;
										Toast.makeText(MainActivity.this, "Press again to exit", Toast.LENGTH_SHORT).show();
									}
								}
							});
						});
			} catch (Exception ignored) { }
		}
	}
	private boolean allPermissionsGranted() {
		for (String permission : REQUIRED_PERMISSIONS) {
			if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
				return false;
			}
		}
		return true;
	}
}