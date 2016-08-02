package com.chatt.demo;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.text.format.DateUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.chatt.demo.custom.CustomActivity;
import com.chatt.demo.model.Conversation;
import com.chatt.demo.model.Photos;
import com.chatt.demo.utils.Const;
import com.csbm.BEException;
import com.csbm.BEFile;
import com.csbm.BEObject;
import com.csbm.BEQuery;
import com.csbm.FindCallback;
import com.csbm.GetCallback;
import com.csbm.GetDataCallback;
import com.csbm.SaveCallback;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class Chat extends CustomActivity {

    /**
     * The Conversation list.
     */
    private ArrayList<Conversation> convList;

    List<Photos> myImgs = new ArrayList<>();

    /**
     * The chat adapter.
     */
    private ChatAdapter adp;

    /**
     * The Editext to compose the message.
     */
    private EditText txt;

    String strImgId;

//    private ImageView imgView;

    /**
     * The user name of buddy.
     */
    private String buddy;

    /**
     * The date of last message in conversation.
     */
    private Date lastMsgDate;

    /**
     * Flag to hold if the activity is running or not.
     */
    private boolean isRunning;

    /**
     * The handler.
     */
    private static Handler handler;

    Button btnImage;
    Button btnSend;
    GridView scrollView;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat);

        scrollView = (GridView) findViewById(R.id.imgsList);
        scrollView.setVisibility(View.INVISIBLE);
        scrollView.setVisibility(View.GONE);

        convList = new ArrayList<>();
        ListView list = (ListView) findViewById(R.id.list);
        adp = new ChatAdapter();
        list.setAdapter(adp);
        list.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        list.setStackFromBottom(true);

        txt = (EditText) findViewById(R.id.txt);


        txt.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_MULTI_LINE);

        btnSend = (Button) findViewById(R.id.btnSend);
        btnImage = (Button) findViewById(R.id.btnImage);


        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
        btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePopup();
            }
        });

        buddy = getIntent().getStringExtra(Const.EXTRA_DATA);
        getActionBar().setTitle(buddy);

        handler = new Handler();

    }


    @Override
    protected void onResume() {
        super.onResume();
        isRunning = true;
        loadConversationList();

    }

    @Override
    protected void onStart() {
        super.onStart();
        new loadImage().execute("");
    }

    @Override
    protected void onPause() {
        super.onPause();
        isRunning = false;
    }



    private void showImagePopup() {

        if (scrollView.getVisibility() == View.VISIBLE){
            scrollView.setVisibility(View.INVISIBLE);
            scrollView.setVisibility(View.GONE);
        } else {
            scrollView.setVisibility(View.VISIBLE);
        }

    }


    private void sendMessage() {
        if (txt.length() == 0 && strImgId == null)
            return;

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(txt.getWindowToken(), 0);

        String s = txt.getText().toString();
        // get string id
        // if string != null save  list conversation with stringid img

        final Conversation c = new Conversation(s, strImgId, new Date(),
                UserList.user.getUsername());
        c.setStatus(Conversation.STATUS_SENDING);
        convList.add(c);
        adp.notifyDataSetChanged();
        txt.setText(null);

        BEObject po = new BEObject(getString(R.string.Chat));
        po.put(getString(R.string.sender), UserList.user.getUsername());
        po.put(getString(R.string.receiver), buddy);
        po.put(getString(R.string.message), s);
        if (strImgId == null){
            // do nothing
        } else {
            po.put(getString(R.string.imgId), strImgId);
        }

        po.saveEventually(new SaveCallback() {

            @Override
            public void done(BEException e) {
                if (e == null) {
                    c.setStatus(Conversation.STATUS_SENT);
                    // ring tone
//                    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//                    Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
//                    r.play();


                } else {
                    c.setStatus(Conversation.STATUS_FAILED);
                    System.out.println(e.getCode() + " - - - " + e.getMessage());

                }
                adp.notifyDataSetChanged();
            }
        });
        strImgId = null;
    }


    private void loadConversationList() {
        BEQuery<BEObject> q = BEQuery.getQuery(getString(R.string.Chat));
        if (convList.size() == 0) {
            // load all messages...
            ArrayList<String> al = new ArrayList<String>();
            al.add(buddy);
            al.add(UserList.user.getUsername());
            q.whereContainedIn(getString(R.string.sender), al);
            q.whereContainedIn(getString(R.string.receiver), al);
        } else {
            // load only newly received message..
            if (lastMsgDate != null) {
                BEQuery<BEObject> query = BEQuery.getQuery(getString(R.string.Chat));
                query.whereGreaterThan(getString(R.string.createdAt), lastMsgDate);
                query.whereEqualTo(getString(R.string.sender), buddy);
                query.whereEqualTo(getString(R.string.receiver), UserList.user.getUsername());

                List<BEObject> newMsg = null;
                try {
                    newMsg = query.find();
                } catch (BEException e) {
                    e.printStackTrace();
                }

                if (newMsg.size() != 0) {

//                    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//                    Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
//                    r.play();
                }
            }
            q.whereGreaterThan(getString(R.string.createdAt), lastMsgDate);
            q.whereEqualTo(getString(R.string.sender), buddy);
            q.whereEqualTo(getString(R.string.receiver), UserList.user.getUsername());
        }
        q.orderByDescending(getString(R.string.createdAt));
        q.setLimit(30);
        q.findInBackground(new FindCallback<BEObject>() {

            @Override
            public void done(List<BEObject> li, BEException e) {
                if (li != null && li.size() > 0) {
                    for (int i = li.size() - 1; i >= 0; i--) {
                        BEObject po = li.get(i);
                        Conversation c = new Conversation(po
                                .getString(getString(R.string.message)), po.getString(getString(R.string.imgId)),
                                po.getCreatedAt(), po.getString(getString(R.string.sender)));
                        convList.add(c);
                        if (lastMsgDate == null
                                || lastMsgDate.before(c.getDate()))
                            lastMsgDate = c.getDate();
                        adp.notifyDataSetChanged();
                    }
                }
                handler.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        if (isRunning)
                            loadConversationList();
                    }
                }, 2000);
            }
        });

    }


    private class ChatAdapter extends BaseAdapter {


        @Override
        public int getCount() {
            return convList.size();
        }


        @Override
        public Conversation getItem(int arg0) {
            return convList.get(arg0);
        }


        @Override
        public long getItemId(int arg0) {
            return arg0;
        }


        @Override
        public View getView(int pos, View v, ViewGroup arg2) {
            Conversation c = getItem(pos);
            if (c.isSent()) {
                v = getLayoutInflater().inflate(R.layout.chat_item_sent, null);
            } else
                v = getLayoutInflater().inflate(R.layout.chat_item_rcv, null);

            // set time
            TextView lbl = (TextView) v.findViewById(R.id.lbl1);
            final ImageView imgView = (ImageView) v.findViewById(R.id.imgview);
            lbl.setText(DateUtils.getRelativeDateTimeString(Chat.this, c
                            .getDate().getTime(), DateUtils.SECOND_IN_MILLIS,
                    DateUtils.DAY_IN_MILLIS, 0));

            //set msg
            if (c.getMsg() != null) {
                lbl = (TextView) v.findViewById(R.id.lbl2);
                lbl.setText(c.getMsg());
            }


            if (c.getStrImg() != null) {
                final String imgId = c.getStrImg();

                BEQuery<BEObject> query = BEQuery.getQuery(getString(R.string.ImageUpload));
                query.getInBackground(c.getStrImg(), new GetCallback<BEObject>() {
                    @Override
                    public void done(BEObject beObject, BEException e) {
                        if (e == null) {
                            progressDialog = ProgressDialog.show(Chat.this, "",
                                    "Loading file in conversation ...", true);
//                        BEFile fileObject = (BEFile) img.get("hinhanh");
                            BEFile fileObject = (BEFile) beObject.get(getString(R.string.ImageFile));
                            fileObject.getDataInBackground(new GetDataCallback() {
                                @Override
                                public void done(byte[] data, BEException ex) {
                                    if (ex == null) {
                                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
//                                imgView = (ImageView) findViewById(R.id.imgview);
                                       Bitmap newScale = Bitmap.createScaledBitmap(bmp, 100, 100, false);

                                        imgView.setImageBitmap(newScale);
                                        imgView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                                startActivity(new Intent(Chat.this, FullImageActivity.class)
                                                        .putExtra(Const.IMAGE_ID, imgId));
                                                finish();
                                            }
                                        });

                                    } else {
                                        System.out.println(ex.getMessage() + "+++LOAD BITMAP+++++" + ex.getCode());
                                    }
                                }
                            });
                        } else {
                            System.out.println(e.getMessage() + "===LOAD FILE==" + e.getCode() + e.getCause());
                        }
                        progressDialog.dismiss();
                    }
                });
                //set view
//                loadImage(c.getStrImg());
            } else {
                imgView.setVisibility(View.INVISIBLE);
                imgView.setVisibility(View.GONE);
            }
            //check status send msg
            lbl = (TextView) v.findViewById(R.id.lbl3);
            if (c.isSent()) {
                if (c.getStatus() == Conversation.STATUS_SENT)
                    lbl.setText(getString(R.string.sent));
                else if (c.getStatus() == Conversation.STATUS_SENDING) {
                    lbl.setText(getString(R.string.sending));

                } else
                    lbl.setText(getString(R.string.fail));

            } else
                lbl.setText("");
            return v;
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void populateImgList() {

        BEQuery<BEObject> query = BEQuery.getQuery(getString(R.string.ImageUpload));
        query.findInBackground(new FindCallback<BEObject>() {
            @Override
            public void done(List<BEObject> list, BEException e) {
                if (e == null) {
                    for (final BEObject img : list) {
                        BEFile fileObject = (BEFile) img.get(getString(R.string.ImageFile));
                        fileObject.getDataInBackground(new GetDataCallback() {
                            @Override
                            public void done(byte[] data, BEException ex) {
                                if (ex == null) {
                                    Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                                    //resize bitmap
                                    Bitmap reScale = Bitmap.createScaledBitmap(bmp, 100, 100, false);
                                    myImgs.add(new Photos(reScale, img.getObjectId()));
                                } else {
                                    System.out.println(ex.getMessage() + "++++++++" + ex.getCode());
                                }
                            }
                        });
                    }

                } else {
                    System.out.println(e.getMessage() + "=====" + e.getCode() + e.getCause());
                }
            }
        });


        populateListView();
    }

    //add data to list view
    private void populateListView() {
        ArrayAdapter<Photos> adapter = new MyListAdapter();
        GridView list1 = (GridView) findViewById(R.id.imgsList);
        list1.setAdapter(adapter);
        registerClickCallback();

    }

    // action click position
    private void registerClickCallback() {
        GridView list = (GridView) findViewById(R.id.imgsList);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked,
                                    int position, long id) {
                //set imgID
                Photos clickedImg = myImgs.get(position);

                strImgId = clickedImg.getPhotoId();

                sendMessage();
                scrollView.setVisibility(View.INVISIBLE);
                scrollView.setVisibility(View.GONE);
            }
        });

    }

    private class loadImage extends AsyncTask<String, String, String>{
        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(Chat.this, "", "Loading...", true);
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {

            populateImgList();
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressDialog.dismiss();
//            showImagePopup();
        }
    }


    private class MyListAdapter extends ArrayAdapter<Photos> {
        public MyListAdapter() {
            super(Chat.this, R.layout.photos_list, myImgs);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Make sure we have a view to work with (may have been given null)
            View row = convertView;
            ViewHolder holder;
            if (row == null) {
                row = getLayoutInflater().inflate(R.layout.photos_list, parent, false);
                holder = new ViewHolder();
                holder.image = (ImageView) row.findViewById(R.id.item_icon);
                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }


            Photos item = myImgs.get(position);
            holder.image.setImageBitmap(item.getIconID());
            return row;
        }

        class ViewHolder {
            ImageView image;
        }

    }


}
