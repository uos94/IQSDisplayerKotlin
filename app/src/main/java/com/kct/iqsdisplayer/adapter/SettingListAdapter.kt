import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kct.iqsdisplayer.common.Const
import com.kct.iqsdisplayer.data.SettingItem
import com.kct.iqsdisplayer.databinding.ItemSettingBinding

// 설정 리스트 어댑터
class SettingListAdapter : RecyclerView.Adapter<SettingListAdapter.ViewHolder>() {

    // 설정 항목 데이터 (예시)
    private val settingItems = listOf(
        SettingItem("표시기 환경설정", 	"",										 ""),
        SettingItem("표시기IP", 			"순번표시기(클라이언트)의 IP를 입력해주세요",	 Const.Key.DisplaySetting.DISPLAYER_IP),
        SettingItem("사운드파일위치", 	"사운드파일 위치를 입력해주세요(상대경로)",	 Const.Key.DisplaySetting.PATH_SOUND_FILE),
        SettingItem("홍보영상파일위치",	"홍보영상파일 위치를 입력해주세요(상대경로)",	 Const.Key.DisplaySetting.PATH_VIDEO_FILE),
        SettingItem("이미지파일위치", 	"이미지파일 위치를 입력해주세요(상대경로)",	 Const.Key.DisplaySetting.PATH_IMG_FILE),
        SettingItem("순번발행기 환경설정", "",										 ""),
        SettingItem("발행기IP", 			"순번발행기(서버)의 IP를 입력해주세요",		 Const.Key.DisplaySetting.IQS_IP),
        SettingItem("발행기PORT", 		"순번발행기(서버)의 PORT를 입력해주세요",		 Const.Key.DisplaySetting.IQS_PORT),
        SettingItem("백업서버IP", 		"백업서버IP를 입력해주세요",					 Const.Key.DisplaySetting.BK_SERVER_IP),
        SettingItem("백업서버PORT",	 	"백업서버의 PORT를 입력해주세요",			 Const.Key.DisplaySetting.BK_SERVER_PORT),
        SettingItem("호출화면", 			"기본호출화면:0 확대호출화면:1 보조표시기화면:2",Const.Key.DisplaySetting.CALL_VIEW)

    )
    // ... 필요한 만큼 설정 항목 추가
    interface OnItemClickListener {
        fun onItemClick(position: Int, item: SettingItem)
    }

    private var onItemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.onItemClickListener = listener
    }

    inner class ViewHolder(private val binding: ItemSettingBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SettingItem) {
            binding.tvMain.text = item.mainText
            binding.tvSub.text = item.subText
            // 아이템 클릭 리스너 설정
            itemView.setOnClickListener {
                onItemClickListener?.onItemClick(adapterPosition, item)
            }

            if (item.isTitleItem()) { // 타이틀 아이템일 경우
                binding.tvSub.apply {
                    setTextColor(Color.BLACK)
                    setPadding(0, 0, 0, 0)
                    paintFlags = paintFlags or Paint.FAKE_BOLD_TEXT_FLAG // 굵게 표시
                }

                binding.tvMain.text = ""
                binding.tvSub.text = item.mainText
                binding.line.setBackgroundColor(Color.DKGRAY)
            } else { // 리스트 아이템일 경우
                binding.tvSub.apply {
                    setTextColor(Color.DKGRAY)
                    setPadding(100, 0, 0, 0)
                    paintFlags = paintFlags and Paint.FAKE_BOLD_TEXT_FLAG.inv() // 굵게 표시 해제
                }

                binding.line.setBackgroundColor(Color.LTGRAY)
                binding.tvMain.text = item.mainText
                binding.tvSub.text = item.subText
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemSettingBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return settingItems.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = settingItems[position]
        holder.bind(item)
    }
}
