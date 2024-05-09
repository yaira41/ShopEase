package com.example.shopease.recipes

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shopease.activities.BaseActivity
import com.example.shopease.R
import com.example.shopease.dataClasses.Recipe
import com.example.shopease.dbHelpers.RecipesDatabaseHelper
import com.example.shopease.dbHelpers.RequestsDatabaseHelper
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton

class RecipesFragment : Fragment() {

    private val recipes: MutableList<Recipe> = mutableListOf()
    private lateinit var recipesAdapter: RecipesAdapter
    private lateinit var username: String
    private lateinit var recipesDatabaseHelper: RecipesDatabaseHelper
    private lateinit var requestsDatabaseHelper: RequestsDatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as BaseActivity?)?.updateTitle("התבשילים שלי")
        val view = inflater.inflate(R.layout.fragment_my_recipes, container, false)
        username = arguments?.getString("USERNAME_KEY") ?: ""
        recipesDatabaseHelper = RecipesDatabaseHelper()
        requestsDatabaseHelper = RequestsDatabaseHelper()

        recipesAdapter = RecipesAdapter(
            recipes,
            itemClickListener = object : RecipesAdapter.OnItemClickListener {
                override fun onItemClick(position: Int) {
                    val selectedRecipe = recipes[position]
                    val bundle = Bundle()
                    bundle.putString("RECIPE_ID_KEY", selectedRecipe.id)
                    bundle.putString("RECIPE_NAME_KEY", selectedRecipe.name)
                    bundle.putString("USERNAME_KEY", username)
                    replaceWithNewFragment(RecipeFragment(), bundle)
                }
            },
            itemLongClickListener = object : RecipesAdapter.OnItemLongClickListener {
                override fun onItemLongClick(position: Int, view: View) {
                    // Show the update dialog on long press
                    showUpdateItemDialog(recipes[position], position)
                }
            },

            view
        )

        fetchUserRecipes(username)

        val fab = view.findViewById<FloatingActionButton>(R.id.bCreateRecipe)
        fab.setOnClickListener {
            showCreateRecipesDialog()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvRecipes = view.findViewById<RecyclerView>(R.id.rvAllRecipes)
        rvRecipes.adapter = recipesAdapter
        rvRecipes.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun showShareRecipeDialog(selectedRecipe: Recipe) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("בחר עם מי לשתף.")

        // Use the asynchronous getFriendsFromUsername function
        requestsDatabaseHelper.getFriendsFromUsername(username) { friendUsernames ->
            val checkedFriends = BooleanArray(friendUsernames.size) { false }

            builder.setMultiChoiceItems(
                friendUsernames.toTypedArray(),
                checkedFriends
            ) { _, which, checked ->
                checkedFriends[which] = checked
            }

            builder.setPositiveButton("שתף") { _, _ ->
                val selectedFriends = mutableListOf<String>()
                selectedFriends.add(username) // Add itself first
                for (i in checkedFriends.indices) {
                    if (checkedFriends[i]) {
                        selectedFriends.add(friendUsernames[i])
                    }
                }

                shareRecipeWithFriends(selectedRecipe, selectedFriends)
            }

            builder.setNegativeButton("ביטול") { dialog, _ ->
                dialog.cancel()
            }

            builder.show()
        }
    }

    private fun shareRecipeWithFriends(selectedRecipe: Recipe, selectedFriends: List<String>) {
        recipesDatabaseHelper.updateRecipe(selectedRecipe.id!!,
            selectedRecipe.name,
            selectedRecipe.items!!,
            selectedFriends,
            selectedRecipe.procedure,
            object : RecipesDatabaseHelper.InsertRecipeCallback {
                override fun onRecipeInserted(recipe: Recipe?) {
                    if (recipe != null) {
                        showToast("הרשימה שותפה בהצלחה.")
                    } else {
                        showToast("משהו השתבש.")
                    }
                }
            })
    }

    private fun showUpdateItemDialog(selectedRecipe: Recipe, position: Int) {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = LayoutInflater.from(requireContext())
        val dialogView = inflater.inflate(R.layout.update_item_dialog, null)
        val editText = dialogView.findViewById<EditText>(R.id.changeWishlistName)
        val confirmButton = dialogView.findViewById<MaterialButton>(R.id.updateWishlistName)
        val deleteButton = dialogView.findViewById<MaterialButton>(R.id.deleteWishlistButton)
        val shareRecipeButton = dialogView.findViewById<MaterialButton>(R.id.sharedListButton)
        editText.setText(selectedRecipe.name)

        builder.setView(dialogView)
        val alertDialog = builder.create()

        confirmButton.setOnClickListener {
            val updatedName = editText.text.toString()
            if (updatedName.isNotEmpty()) {
                recipes[position].name = updatedName
                val id = recipes[position].id
                recipesDatabaseHelper.updateRecipeName(id!!, updatedName)
                recipesAdapter.notifyItemChanged(position)
            }
            alertDialog.dismiss()
        }
        shareRecipeButton.setOnClickListener {
            showShareRecipeDialog(selectedRecipe)
        }

        deleteButton.setOnClickListener {
            showConfirmationDialog(position)
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    private fun fetchUserRecipes(userName: String) {
        recipesDatabaseHelper.getAllUserRecipes(userName) { allRecipes ->
            recipesAdapter.clear()
            if (allRecipes.isEmpty()) {
                Toast.makeText(requireContext(), "נראה שאין לך תבשילים", Toast.LENGTH_SHORT).show()
            } else {
                recipesAdapter.initialList(allRecipes)
            }
        }
    }

    private fun onDeleteButtonClick(position: Int) {
        val selectedRecipe = recipes[position]
        Toast.makeText(requireContext(), "Delete ${selectedRecipe.name}", Toast.LENGTH_SHORT).show()
        if (!selectedRecipe.id.isNullOrEmpty()) {
            recipesDatabaseHelper.deleteRecipeForSpecificUser(selectedRecipe.id, username)
        }

        // Remove the item from the data source
        recipes.removeAt(position)
        // Notify the adapter about the item removal
        recipesAdapter.notifyItemRemoved(position)

        // Update the positions in the adapter for items after the deleted one
        for (i in position until recipes.size) {
            recipesAdapter.notifyItemChanged(i)
        }
    }

    private fun replaceWithNewFragment(newFragment: Fragment, args: Bundle? = null) {
        newFragment.arguments = args

        parentFragmentManager.beginTransaction().replace(R.id.fragmentContainer, newFragment)
            .addToBackStack(null).commit()
    }

    private fun showCreateRecipesDialog() {
        val context = context ?: return  // Check if the fragment is attached to a context
        val builder = AlertDialog.Builder(context)
        builder.setTitle("שם תבשיל")

        val input = EditText(context)
        builder.setView(input)

        builder.setPositiveButton("צור") { _, _ ->
            val recipeName = input.text.toString()
            if (recipeName.isEmpty()) {
                showToast("הכנס שם לתבשיל")
            } else {
                recipesDatabaseHelper.insertNewRecipe(
                    recipeName,
                    null,
                    listOf(username),
                    "",
                    object : RecipesDatabaseHelper.InsertRecipeCallback {
                        override fun onRecipeInserted(recipe: Recipe?) {
                            if (recipe != null) {
                                showToast("התבשיל נוצר בהצלחה.")
                                recipesAdapter.addRecipe(recipe)
                            } else {
                                showToast("משהו השתבש.")
                            }
                        }
                    }
                )
            }
        }

        builder.setNegativeButton("בטל") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun showToast(message: String) {
        val context = context
        if (context != null) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showConfirmationDialog(position: Int) {
        val dialogView = layoutInflater.inflate(R.layout.confirmation_dialog, null)
        val builder = AlertDialog.Builder(requireContext())
        builder.setView(dialogView)
        val dialog = builder.create()

        val confirmButton: Button = dialogView.findViewById(R.id.btnConfirmDelete)
        val cancelButton: Button = dialogView.findViewById(R.id.btnCancelDelete)

        confirmButton.setOnClickListener {
            onDeleteButtonClick(position)
            dialog.dismiss()
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}
