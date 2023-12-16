import android.app.AlertDialog
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.kotlindeneme4.R
import com.example.kotlindeneme4.model.UserModel
import com.google.firebase.firestore.FirebaseFirestore

class UserListAdapter(private var userList: List<UserModel>, private val currentUserUid: String?) : RecyclerView.Adapter<UserListAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val usernameTextView: TextView = itemView.findViewById(R.id.user_isim_view)
        val emailTextView: TextView = itemView.findViewById(R.id.user_mail_view)
        val rolesTextView: TextView = itemView.findViewById(R.id.user_roles_view)
        val profilePicImageView: ImageView = itemView.findViewById(R.id.user_image_view)
        val changeRoleButton: Button = itemView.findViewById(R.id.changeRoleButton)
        val blockButton: Button = itemView.findViewById(R.id.blockButton)
    }

    // UI güncellemelerini yönetmek için setUi fonksiyonunu ekleyin
    private fun setUi(holder: ViewHolder, user: UserModel) {
        holder.usernameTextView.text = user.username
        holder.emailTextView.text = user.email
        holder.rolesTextView.text = user.roles

        Glide.with(holder.itemView.context)
            .load(user.profilePic)
            .placeholder(R.drawable.icon_account_circle)
            .circleCrop()
            .into(holder.profilePicImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.user_item_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentUser = userList[position]

        // setUi fonksiyonunu kullanarak UI'yi güncelle
        setUi(holder, currentUser)

        // Sadece mevcut kullanıcının kendi profilini düzenlemesine izin ver
        if (currentUser.id != currentUserUid) {
            holder.changeRoleButton.isEnabled = true
            holder.blockButton.isEnabled = true
        } else {
            holder.changeRoleButton.visibility = View.GONE
            holder.blockButton.visibility = View.GONE
        }

        holder.changeRoleButton.setOnClickListener {
            showRoleSelectionDialog(holder, currentUser)
        }

        if (currentUser.block) {
            holder.blockButton.text = "Engeli Kaldır"
            holder.blockButton.backgroundTintList = ColorStateList.valueOf(holder.itemView.resources.getColor(R.color.red))
        } else {
            holder.blockButton.text = "Engelle"
            holder.blockButton.backgroundTintList = ColorStateList.valueOf(holder.itemView.resources.getColor(R.color.green))
        }

        holder.blockButton.setOnClickListener {
            toggleBlockedStatus(holder, currentUser)
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    // Listeyi güncelleyen bir fonksiyon ekleyin
    fun updateList(newList: List<UserModel>) {
        // Mevcut kullanıcının pozisyonunu bulun
        val currentUserPosition = newList.indexOfFirst { it.id == currentUserUid }

        // Mevcut kullanıcının pozisyonunu tespit ettiyseniz
        if (currentUserPosition != -1) {
            // Mevcut kullanıcıyı listenin başına alın
            val updatedList = mutableListOf<UserModel>()
            updatedList.add(newList[currentUserPosition])

            // Listenin geri kalanını ekleyin (mevcut kullanıcı hariç)
            updatedList.addAll(newList.filterIndexed { index, _ -> index != currentUserPosition })

            // Güncellenmiş listeyi kullanın
            userList = updatedList
            notifyDataSetChanged()
        }
    }

    private fun showRoleSelectionDialog(holder: ViewHolder, currentUser: UserModel) {
        val roles = arrayOf("User", "Admin")
        val builder = AlertDialog.Builder(holder.itemView.context)
        builder.setTitle("Rolü Seç")
        builder.setItems(roles) { dialog, which ->
            val selectedRole = roles[which]

            FirebaseFirestore.getInstance().collection("users")
                .document(currentUser.id)
                .update("roles", selectedRole)
                .addOnSuccessListener {
                    currentUser.roles = selectedRole
                    // setUi fonksiyonunu kullanarak UI'yi güncelle
                    setUi(holder, currentUser)
                    Toast.makeText(holder.itemView.context, "Rol güncellendi: $selectedRole", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(holder.itemView.context, "Rol güncelleme hatası: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
        builder.show()
    }

    private fun toggleBlockedStatus(holder: ViewHolder, currentUser: UserModel) {
        // Toggle the blocked status
        val newBlockedStatus = !currentUser.block

        // Update the Firestore document with the new blocked status
        FirebaseFirestore.getInstance().collection("users")
            .document(currentUser.id)
            .update("block", newBlockedStatus)
            .addOnSuccessListener {
                currentUser.block = newBlockedStatus

                // Update UI
                setUi(holder, currentUser)

                // Update the button text and background tint
                if (newBlockedStatus) {
                    holder.blockButton.text = "Engeli Kaldır"
                    holder.blockButton.backgroundTintList = ColorStateList.valueOf(holder.itemView.resources.getColor(R.color.red))
                } else {
                    holder.blockButton.text = "Engelle"
                    holder.blockButton.backgroundTintList = ColorStateList.valueOf(holder.itemView.resources.getColor(R.color.green))
                }

                Toast.makeText(holder.itemView.context, "Engelleme durumu güncellendi", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(holder.itemView.context, "Engelleme durumu güncelleme hatası: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
