package peace_shillong.kint;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.os.Environment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import peace_shillong.model.DatabaseManager;
import peace_shillong.model.PosParser;
import peace_shillong.model.Word;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class VerseFragment extends Fragment {

    private String book;
    private int chapter;
    private int verse;
    private List<Word> words;
    private Typeface typeface;
    private ActivityObjectProvider provider;
    private View contentView;
    private ScrollView screen;
    //private FlowLayout layout;
    private Context context;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public static VerseFragment newInstance(String book, int chapter, int verse) {
        VerseFragment fragment = new VerseFragment();
        fragment.setRetainInstance(true);
        Bundle args = new Bundle();

        args.putString("book", book);
        args.putInt("chapter", chapter);
        args.putInt("verse", verse);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try
        {
            provider = (ActivityObjectProvider) activity;
        } catch(ClassCastException e) {
            throw new RuntimeException("it ain't a Provider");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        book = getArguments().getString("book");
        chapter = getArguments().getInt("chapter");
        verse = getArguments().getInt("verse");
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if(isVisibleToUser) {
            FragmentActivity activity = getActivity();
            if(activity != null)
            {
                ((MainActivity)activity).setActionBarTitle(String.format("%s %d:%d", book, chapter, verse));
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = container.getContext();
        contentView = inflater.inflate(R.layout.activity_fragment, null);
        setHasOptionsMenu(true);
        typeface = provider.getTypeface();
        words = provider.getWords(verse);

        FlowLayout layout = new FlowLayout(context, null);
        layout.setLayoutParams(
                new FlowLayout.LayoutParams(
                        FlowLayout.LayoutParams.MATCH_PARENT,
                        FlowLayout.LayoutParams.WRAP_CONTENT
                ));
        layout.setPadding(10, 10, 10, 10);

        List<Word> words = provider.getWords(verse);
        if(words == null) {
            LinearLayout linearLayout = new LinearLayout(context);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout.setPadding(32, 32, 32, 32);
            linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));

            TextView textViewErrorText = new TextView(context);
            textViewErrorText.setTextAppearance(context, android.R.style.TextAppearance_Medium);
            textViewErrorText.setText(context.getString(R.string.missing_verse_text));

            TextView textViewErrorLink = new TextView(context);
            //textViewErrorLink.setText(context.getString(R.string.missing_verse_link));
            textViewErrorLink.setText(Html.fromHtml("For more information <a href='http://en.wikipedia.org/wiki/List_of_Bible_verses_not_included_in_modern_translations'>this link</a>"));
            textViewErrorLink.setTextAppearance(context, android.R.style.TextAppearance_Medium);
            textViewErrorLink.setMovementMethod(LinkMovementMethod.getInstance());

            linearLayout.addView(textViewErrorText);
            linearLayout.addView(textViewErrorLink);

            ((ViewGroup) contentView).addView(linearLayout);
        }
        else {
            int count = words.size();
            for (int i = 0; i < count; i++) {

                LinearLayout linearLayout = getLayout(context, i);

                layout.addView(linearLayout);
            }
            ((ViewGroup) contentView).addView(layout);
        }
        screen=contentView.findViewById(R.id.screen);
        return contentView; // super.onCreateView(inflater, container, savedInstanceState);
    }

    private LinearLayout getLayout(Context context, int index) {

        Bundle prefs = provider.getPreferences();

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setPadding(10, 20, 30, 40);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));

        final TextView textViewStrongs = new TextView(context);
        textViewStrongs.setTag("textViewStrongs" + index);
        textViewStrongs.setTextAppearance(context, android.R.style.TextAppearance_Small);
        textViewStrongs.setTextColor(Color.rgb(77, 179, 179));
        textViewStrongs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = textViewStrongs.getText().toString();
                String[] parts = text.split("\\&");

                DatabaseManager manager = DatabaseManager.getInstance();
                String strongs = manager.getStrongs(parts);

                Toast toast = Toast.makeText(v.getContext(), strongs, Toast.LENGTH_LONG);
                toast.show();
            }
        });

        TextView textViewWord = new TextView(context);
        textViewWord.setTextAppearance(context, android.R.style.TextAppearance_Large);
        textViewWord.setTypeface(typeface);
        textViewWord.setTextColor(Color.rgb(61, 76, 83));
        textViewWord.setTag("textViewWord" + index);

        TextView textViewConcordance = new TextView(context);
        textViewConcordance.setTextAppearance(context, android.R.style.TextAppearance_Holo_Small);
        textViewConcordance.setTextColor(Color.rgb(230, 74, 69));
        textViewConcordance.setTag("textViewConcordance" + index);

        TextView textViewFunctional = new TextView(context);
        textViewFunctional.setTextAppearance(context, android.R.style.TextAppearance_Small);
        textViewFunctional.setTextColor(Color.rgb(77, 179, 179));
        textViewFunctional.setTag("textViewFunctional" + index);
        textViewFunctional.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String result = PosParser.get((String) ((TextView) v).getText());

                Toast toast = Toast.makeText(v.getContext(), result, Toast.LENGTH_LONG);
                toast.show();
            }
        });

        textViewStrongs.setText(words.get(index).getStrongs());
        textViewWord.setText(words.get(index).getWord());
        textViewFunctional.setText(words.get(index).getFunctional());
        textViewConcordance.setText(words.get(index).getConcordance());

        boolean showStrongs = prefs.getBoolean("show_strongs", false);
        if(showStrongs)
            linearLayout.addView(textViewStrongs);

        linearLayout.addView(textViewWord);

        boolean showConcordance = prefs.getBoolean("show_concordance", false);
        if(showConcordance)
            linearLayout.addView(textViewConcordance);

        boolean showFunctional = prefs.getBoolean("show_functional", false);
        if(showFunctional)
            linearLayout.addView(textViewFunctional);

        return linearLayout;
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Do something that differs the Activity's menu here
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Log.d("LOG", "Got called");
        switch (item.getItemId()) {
            case R.id.action_export:
                // Do Fragment menu item stuff here
                Toast.makeText(context, "Exporting Image...", Toast.LENGTH_SHORT).show();
                screenshot("export");
                return true;
            case R.id.action_share:
                // Do Fragment menu item stuff here
                screenshot("share");
                Toast.makeText(context, "Generating Image...", Toast.LENGTH_SHORT).show();
                return true;
            default:
                break;
        }
        return false;
    }

    private void screenshot(String action) {
        screen.setDrawingCacheEnabled(true);
        try {
            Bitmap bitmap = getBitmapFromView(screen, screen.getChildAt(0).getHeight(), screen.getChildAt(0).getWidth());
            //Create File and  then store in Downloads Folder
            File file = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS), "NT "+ book+" Chapter "+chapter+" Verse "+verse+".jpg");
            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            stream.flush();
            stream.close();
            if(action.equals("share")) {
                //if SDK below else
                //Uri uri = Uri.fromFile(file);
                Uri uri  = FileProvider.getUriForFile(context, "com.peace_shillong.kint.provider", file);
                //share
                Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_TEXT, book+" "+chapter+":"+verse+" in Greek");
                startActivity(Intent.createChooser(intent, "Share Verse"));

            }
            //Log.d("LOG", "Image created");
            if(action.equals("export"))
            {
                Toast.makeText(context, "Image Saved in Downloads Folder", Toast.LENGTH_SHORT).show();
            }
            //screen.destroyDrawingCache();
        } catch (Exception e) {
            //e.printStackTrace();
            Toast.makeText(context,"Unable to Save Image to Storage, Please check app permission",Toast.LENGTH_SHORT).show();
            //Log.d("LOG", "Image is too large, Please Crop the image with a smaller size");
        }
    }
    //create bitmap from the ScrollView
    private Bitmap getBitmapFromView(View view, int height, int width) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);//added this line of code for bg to be WHITE if screen not scrolled
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null)
            bgDrawable.draw(canvas);
        else
            canvas.drawColor(Color.WHITE);
        view.draw(canvas);
        return bitmap;
    }
}
