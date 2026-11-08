# حاسب فاتورتي DZ

تطبيق أندرويد مساعد لتقدير فواتير الخدمات في الجزائر:
- سونلغاز: الكهرباء والغاز في فاتورة موحدة.
- الماء: ADE وشركات المياه.


## الخصوصية
الإصدار الحالي يعالج المدخلات محلياً على الجهاز ولا يرسل قراءات العدادات أو مبالغ الفواتير إلى خوادم المطوّر. راجع [سياسة الخصوصية](docs/privacy-policy.html).

## تنبيه حول الحسابات
التطبيق أداة تقدير ومساعدة، وليس خدمة رسمية تابعة لسونلغاز أو الجزائرية للمياه أو اتصالات الجزائر. قد تختلف النتائج بسبب التقريب، نوع العقد، المنطقة، الضرائب أو بنود الفاتورة الخاصة. يجب مقارنة النتيجة بالفاتورة الرسمية قبل الدفع. لا يُضمن التطابق التام لكل الحالات.

## Build
- JDK 17
- Android Gradle Plugin 8.10.1
- Gradle 8.11.1
- compileSdk / targetSdk 36
- minSdk 24 (Android 7.0)
- Google Mobile Ads Next-Gen + UMP consent support

## Advertising
See [AdMob setup guide](docs/admob-setup.md). Debug builds use Google's test ad IDs. Release ads remain inactive until both valid production AdMob IDs are configured as GitHub Actions secrets (`ADMOB_APP_ID` and `ADMOB_BANNER_AD_UNIT_ID`).


```bash
gradle testDebugUnitTest
gradle assembleDebug
gradle bundleRelease
```

## Google Play release signing
To sign the release bundle in GitHub Actions, configure these repository Actions secrets:
- `PLAY_UPLOAD_KEYSTORE_BASE64`
- `PLAY_UPLOAD_KEYSTORE_PASSWORD`
- `PLAY_UPLOAD_KEY_ALIAS`
- `PLAY_UPLOAD_KEY_PASSWORD`

Without these secrets, the workflow can compile an unsigned AAB for validation, but it is not ready to upload to Play Console.
