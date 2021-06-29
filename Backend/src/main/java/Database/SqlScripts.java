package Database;

public class SqlScripts {
    public static String dropScript = "DO $$ DECLARE\n" +
            "  r RECORD;\n" +
            "BEGIN\n" +
            "  FOR r IN (SELECT tablename FROM pg_tables WHERE schemaname = current_schema()) LOOP\n" +
            "    EXECUTE 'DROP TABLE ' || quote_ident(r.tablename) || ' CASCADE';\n" +
            "  END LOOP;\n" +
            "END $$;\n"    ;

    public static String createTablesScript = "CREATE TABLE users(\n" +
            "\"id\" int PRIMARY KEY GENERATED ALWAYS AS IDENTITY,\n" +
            "email varchar(200) UNIQUE NOT NULL,\n" +
            "\"password\" varchar(200) NOT NULL,\n" +
            "is_banned boolean DEFAULT FALSE,\n" +
            "is_premium boolean DEFAULT FALSE,\n" +
            "credit_card_token nchar(200),\n" +
            "first_name varchar(200) NOT NULL,\n" +
            "last_name varchar(200) NOT NULL,\n" +
            "CONSTRAINT proper_email CHECK (email ~* '^[A-Za-z0-9._%-]+@[A-Za-z0-9.-]+[.][A-Za-z]+$')\n" +
            ");\n" +
            "CREATE TABLE moderators(\n" +
            "\"id\" int PRIMARY KEY GENERATED ALWAYS AS IDENTITY,\n" +
            "email varchar(200) UNIQUE NOT NULL,\n" +
            "\"password\" varchar(200) NOT NULL,\n" +
            "CONSTRAINT proper_email CHECK (email ~* '^[A-Za-z0-9._%-]+@[A-Za-z0-9.-]+[.][A-Za-z]+$')\n" +
            ");\n" +
            "CREATE TABLE INTERESTS(\"id\" int PRIMARY KEY GENERATED ALWAYS AS IDENTITY,\n" +
            "\"name\" NCHAR(200) NOT NULL\n" +
            ");\n" +
            "DO $$ BEGIN\n" +
            "  CREATE TYPE interaction_type AS ENUM('like', 'dislike','super_like');\n" +
            "EXCEPTION\n" +
            "    WHEN duplicate_object THEN null;\n" +
            "END $$;\n" +
            "\n" +
            "CREATE TABLE interactions(\n" +
            "\"id\" int PRIMARY KEY GENERATED ALWAYS AS IDENTITY,\n" +
            "source_user_id int REFERENCES users(\"id\") ON DELETE CASCADE NOT NULL,\n" +
            "target_user_id int REFERENCES users(\"id\") ON DELETE CASCADE NOT NULL,\n" +
            "\"type\" interaction_type NOT NULL,\n" +
            "created_at timestamp DEFAULT current_timestamp\n" +
            ");\n" +
            "CREATE TABLE blocks(\n" +
            "source_user_id int REFERENCES users(\"id\") ON DELETE CASCADE NOT NULL,\n" +
            "target_user_id int REFERENCES users(\"id\") ON DELETE CASCADE NOT NULL,\n" +
            "created_at timestamp DEFAULT current_timestamp,\n" +
            "PRIMARY KEY (source_user_id,target_user_id)\n" +
            ");\n" +
            "CREATE TABLE reports(\n" +
            "\"id\" int PRIMARY KEY GENERATED ALWAYS AS IDENTITY,\n" +
            "source_user_id int REFERENCES users(\"id\") ON DELETE CASCADE NOT NULL,\n" +
            "target_user_id int REFERENCES users(\"id\") ON DELETE CASCADE NOT NULL,\n" +
            "reason varchar(400),\n" +
            "created_at timestamp DEFAULT current_timestamp\n" +
            ");\n" +
            "CREATE TABLE transactions(\n" +
            "\"id\" int PRIMARY KEY GENERATED ALWAYS AS IDENTITY,\n" +
            "user_id int REFERENCES users(\"id\"),\n" +
            "amount money NOT NULL,\n" +
            "created_at timestamp DEFAULT current_timestamp\n" +
            ");\n" +
            "CREATE TABLE bans(\n" +
            "\"id\" int PRIMARY KEY GENERATED ALWAYS AS IDENTITY,\n" +
            "moderator_id int REFERENCES moderators(\"id\")  NOT NULL,\n" +
            "user_id int REFERENCES users(\"id\") ON DELETE CASCADE NOT NULL,\n" +
            "reason varchar(400),\n" +
            "created_at timestamp DEFAULT current_timestamp,\n" +
            "expiry_date date NOT NULL\n" +
            ");";

    public  static  String inserStionScript = "\n" +
            "BEGIN;\n" +
            "-- Users\n" +
            "\n" +
            "\tINSERT INTO public.users(\n" +
            "\temail, password, is_banned, is_premium, credit_card_token, first_name, last_name)\n" +
            "\tVALUES ('ariannagrande@gmail.com', '$2a$12$W5/J6mREYFuQoVxPUEgkUOt050wgiu/i.uKKEjLp6d3eb/83UprMa', false, false, NULL, 'Arianna', 'Grande');\n" +
            "\t\n" +
            "\tINSERT INTO public.users(\n" +
            "\temail, password, is_banned, is_premium, credit_card_token, first_name, last_name)\n" +
            "\tVALUES ('doja.cat@gmail.com', '$2a$12$W5/J6mREYFuQoVxPUEgkUOt050wgiu/i.uKKEjLp6d3eb/83UprMa', false, false, NULL, 'Doja', 'Cat');\n" +
            "\t\n" +
            "\tINSERT INTO public.users(\n" +
            "\temail, password, is_banned, is_premium, credit_card_token, first_name, last_name)\n" +
            "\tVALUES ('billie.eilish@gmail.com', '$2a$12$W5/J6mREYFuQoVxPUEgkUOt050wgiu/i.uKKEjLp6d3eb/83UprMa', false, false, NULL, 'Billie', 'Eilish');\n" +
            "\t\n" +
            "\tINSERT INTO public.users(\n" +
            "\temail, password, is_banned, is_premium, credit_card_token, first_name, last_name)\n" +
            "\tVALUES ('megan.stalyon@gmail.com', '$2a$12$W5/J6mREYFuQoVxPUEgkUOt050wgiu/i.uKKEjLp6d3eb/83UprMa', false, false, NULL, 'Megan', 'The Stalyon');\n" +
            "\t\n" +
            "\tINSERT INTO public.users(\n" +
            "\temail, password, is_banned, is_premium, credit_card_token, first_name, last_name)\n" +
            "\tVALUES ('selena.gomez@gmail.com', '$2a$12$W5/J6mREYFuQoVxPUEgkUOt050wgiu/i.uKKEjLp6d3eb/83UprMa', false, false, NULL, 'Selena', 'Gomez');\n" +
            "\t\n" +
            "\tINSERT INTO public.users(\n" +
            "\temail, password, is_banned, is_premium, credit_card_token, first_name, last_name)\n" +
            "\tVALUES ('justin.bieber@gmail.com', '$2a$12$W5/J6mREYFuQoVxPUEgkUOt050wgiu/i.uKKEjLp6d3eb/83UprMa', false, false, NULL, 'Justin', 'Bieber');\n" +
            "\t\n" +
            "\tINSERT INTO public.users(\n" +
            "\temail, password, is_banned, is_premium, credit_card_token, first_name, last_name)\n" +
            "\tVALUES ('post.malone@gmail.com', '$2a$12$W5/J6mREYFuQoVxPUEgkUOt050wgiu/i.uKKEjLp6d3eb/83UprMa', false, false, NULL, 'Post', 'Malone');\n" +
            "\t\n" +
            "\tINSERT INTO public.users(\n" +
            "\temail, password, is_banned, is_premium, credit_card_token, first_name, last_name)\n" +
            "\tVALUES ('taylor.swift@gmail.com', '$2a$12$W5/J6mREYFuQoVxPUEgkUOt050wgiu/i.uKKEjLp6d3eb/83UprMa', false, true, 5186975364203198, 'Taylor', 'Swift');\n" +
            "\t\n" +
            "\tINSERT INTO public.users(\n" +
            "\temail, password, is_banned, is_premium, credit_card_token, first_name, last_name)\n" +
            "\tVALUES ('dua.lippa@gmail.com', '$2a$12$W5/J6mREYFuQoVxPUEgkUOt050wgiu/i.uKKEjLp6d3eb/83UprMa', false, false, NULL, 'Dua', 'Lippa');\n" +
            "\t\n" +
            "\tINSERT INTO public.users(\n" +
            "\temail, password, is_banned, is_premium, credit_card_token, first_name, last_name)\n" +
            "\tVALUES ('jennifer.lopez@gmail.com', '$2a$12$W5/J6mREYFuQoVxPUEgkUOt050wgiu/i.uKKEjLp6d3eb/83UprMa', false, false, NULL, 'Jennifer', 'Lopez');\n" +
            "\t\n" +
            "\tINSERT INTO public.users(\n" +
            "\temail, password, is_banned, is_premium, credit_card_token, first_name, last_name)\n" +
            "\tVALUES ('mariah.carey@gmail.com', '$2a$12$W5/J6mREYFuQoVxPUEgkUOt050wgiu/i.uKKEjLp6d3eb/83UprMa', false, true, 5192086875353209, 'Mariah', 'Carey');\n" +
            "\t\n" +
            "\tINSERT INTO public.users(\n" +
            "\temail, password, is_banned, is_premium, credit_card_token, first_name, last_name)\n" +
            "\tVALUES ('drake@gmail.com', '$2a$12$W5/J6mREYFuQoVxPUEgkUOt050wgiu/i.uKKEjLp6d3eb/83UprMa', false, true, 5186425319208694, 'Aubrey', 'Drake');\n" +
            "\t\n" +
            "\tINSERT INTO public.users(\n" +
            "\temail, password, is_banned, is_premium, credit_card_token, first_name, last_name)\n" +
            "\tVALUES ('dj.khaled@gmail.com', '$2a$12$W5/J6mREYFuQoVxPUEgkUOt050wgiu/i.uKKEjLp6d3eb/83UprMa', false, false, NULL, 'DJ', 'Khaled');\n" +
            "\t\n" +
            "\tINSERT INTO public.users(\n" +
            "\temail, password, is_banned, is_premium, credit_card_token, first_name, last_name)\n" +
            "\tVALUES ('travis.scott@gmail.com', '$2a$12$W5/J6mREYFuQoVxPUEgkUOt050wgiu/i.uKKEjLp6d3eb/83UprMa', false, false, NULL, 'Travis', 'Scott');\n" +
            "\t\n" +
            "\tINSERT INTO public.users(\n" +
            "\temail, password, is_banned, is_premium, credit_card_token, first_name, last_name)\n" +
            "\tVALUES ('jack.harlow@gmail.com', '$2a$12$W5/J6mREYFuQoVxPUEgkUOt050wgiu/i.uKKEjLp6d3eb/83UprMa', false, false, NULL, 'Jack', 'Harlow');\n" +
            "\t\n" +
            "\tINSERT INTO public.users(\n" +
            "\temail, password, is_banned, is_premium, credit_card_token, first_name, last_name)\n" +
            "\tVALUES ('kanye.west@gmail.com', '$2a$12$W5/J6mREYFuQoVxPUEgkUOt050wgiu/i.uKKEjLp6d3eb/83UprMa', false, true, 5192086975364205, 'Kanye', 'West');\n" +
            "\n" +
            "\tINSERT INTO public.users(\n" +
            "\temail, password, is_banned, is_premium, credit_card_token, first_name, last_name)\n" +
            "\tVALUES ('donald.trump@gmail.com', '$2a$12$W5/J6mREYFuQoVxPUEgkUOt050wgiu/i.uKKEjLp6d3eb/83UprMa', false, false, NULL, 'Donald', 'Trump');\n" +
            "\t\n" +
            "\tINSERT INTO public.users(\n" +
            "\temail, password, is_banned, is_premium, credit_card_token, first_name, last_name)\n" +
            "\tVALUES ('liam.hemsworth@gmail.com', '$2a$12$W5/J6mREYFuQoVxPUEgkUOt050wgiu/i.uKKEjLp6d3eb/83UprMa', false, false, NULL, 'Liam', 'Hemsworth');\n" +
            "\t\n" +
            "\tINSERT INTO public.users(\n" +
            "\temail, password, is_banned, is_premium, credit_card_token, first_name, last_name)\n" +
            "\tVALUES ('miley.cyrus@gmail.com', '$2a$12$W5/J6mREYFuQoVxPUEgkUOt050wgiu/i.uKKEjLp6d3eb/83UprMa', false, false, NULL, 'Miley', 'Cyrus');\n" +
            "\t\n" +
            "----------------------------------------------------------------------------------------------------\n" +
            "\n" +
            "--Moderators\n" +
            "\n" +
            "\tINSERT INTO public.moderators(\n" +
            "\temail, password)\n" +
            "\tVALUES ( 'hussein.badr@gmail.com', '$2a$12$W5/J6mREYFuQoVxPUEgkUOt050wgiu/i.uKKEjLp6d3eb/83UprMa');\n" +
            "\tINSERT INTO public.moderators(\n" +
            "\temail, password)\n" +
            "\tVALUES ( 'youssef.sameh@gmail.com', '$2a$12$W5/J6mREYFuQoVxPUEgkUOt050wgiu/i.uKKEjLp6d3eb/83UprMa');\n" +
            "\n" +
            "----------------------------------------------------------------------------------------------------\n" +
            "\n" +
            "--Interests\n" +
            "\n" +
            "\tINSERT INTO public.interests(\n" +
            "\t name)\n" +
            "\tVALUES ( 'Surfing');\n" +
            "\t\n" +
            "\tINSERT INTO public.interests(\n" +
            "\t name)\n" +
            "\tVALUES ( 'Volunteering');\n" +
            "\t\n" +
            "\tINSERT INTO public.interests(\n" +
            "\t name)\n" +
            "\tVALUES ( 'Tea');\n" +
            "\t\n" +
            "\tINSERT INTO public.interests(\n" +
            "\t name)\n" +
            "\tVALUES ( 'Politics');\n" +
            "\t\n" +
            "\tINSERT INTO public.interests(\n" +
            "\t name)\n" +
            "\tVALUES ( 'Art');\n" +
            "\t\n" +
            "\tINSERT INTO public.interests(\n" +
            "\t name)\n" +
            "\tVALUES ( 'Instagram');\n" +
            "\t\n" +
            "\tINSERT INTO public.interests(\n" +
            "\t name)\n" +
            "\tVALUES ( 'Spirituality');\n" +
            "\t\n" +
            "\tINSERT INTO public.interests(\n" +
            "\t name)\n" +
            "\tVALUES ( 'Dog Lover');\n" +
            "\t\n" +
            "\tINSERT INTO public.interests(\n" +
            "\t name)\n" +
            "\tVALUES ( 'DIY');\n" +
            "\t\n" +
            "\tINSERT INTO public.interests(\n" +
            "\t name)\n" +
            "\tVALUES ( 'Sports');\n" +
            "\t\n" +
            "\tINSERT INTO public.interests(\n" +
            "\t name)\n" +
            "\tVALUES ( 'Cycling');\n" +
            "\t\n" +
            "\tINSERT INTO public.interests(\n" +
            "\t name)\n" +
            "\tVALUES ( 'Foodie');\n" +
            "\t\n" +
            "\tINSERT INTO public.interests(\n" +
            "\t name)\n" +
            "\tVALUES ( 'Astrology');\n" +
            "\t\n" +
            "\tINSERT INTO public.interests(\n" +
            "\t name)\n" +
            "\tVALUES ( 'Netflix');\n" +
            "\t\n" +
            "\tINSERT INTO public.interests(\n" +
            "\t name)\n" +
            "\tVALUES ( 'Photography');\n" +
            "\t\n" +
            "\tINSERT INTO public.interests(\n" +
            "\t name)\n" +
            "\tVALUES ( 'Reading');\t\n" +
            "\t\n" +
            "\t----------------------------------------------------------------------------------------------------\n" +
            "\t\n" +
            "\t--Interactions\n" +
            "\t\n" +
            "\tINSERT INTO public.interactions(\n" +
            "\tsource_user_id, target_user_id, type)\n" +
            "\tVALUES ( 16, 1, 'like');\n" +
            "\t\n" +
            "\tINSERT INTO public.interactions(\n" +
            "\tsource_user_id, target_user_id, type)\n" +
            "\tVALUES ( 16, 12, 'dislike');\n" +
            "\t\n" +
            "\tINSERT INTO public.interactions(\n" +
            "\tsource_user_id, target_user_id, type)\n" +
            "\tVALUES ( 16, 13, 'super_like');\n" +
            "\t\n" +
            "\tINSERT INTO public.interactions(\n" +
            "\tsource_user_id, target_user_id, type)\n" +
            "\tVALUES ( 16, 14, 'dislike');\n" +
            "\t\n" +
            "\tINSERT INTO public.interactions(\n" +
            "\tsource_user_id, target_user_id, type)\n" +
            "\tVALUES ( 16, 15, 'like');\n" +
            "\t\n" +
            "\tINSERT INTO public.interactions(\n" +
            "\tsource_user_id, target_user_id, type)\n" +
            "\tVALUES ( 1, 16, 'like');\n" +
            "\t\n" +
            "\tINSERT INTO public.interactions(\n" +
            "\tsource_user_id, target_user_id, type)\n" +
            "\tVALUES ( 1, 8, 'dislike');\n" +
            "\t\n" +
            "\tINSERT INTO public.interactions(\n" +
            "\tsource_user_id, target_user_id, type)\n" +
            "\tVALUES ( 8, 12, 'dislike');\n" +
            "\t\n" +
            "\tINSERT INTO public.interactions(\n" +
            "\tsource_user_id, target_user_id, type)\n" +
            "\tVALUES ( 11, 15, 'super_like');\n" +
            "\t\n" +
            "\tINSERT INTO public.interactions(\n" +
            "\tsource_user_id, target_user_id, type)\n" +
            "\tVALUES ( 15, 11, 'super_like');\n" +
            "\t\n" +
            "\tINSERT INTO public.interactions(\n" +
            "\tsource_user_id, target_user_id, type)\n" +
            "\tVALUES ( 12, 16, 'like');\n" +
            "\t\n" +
            "\tINSERT INTO public.interactions(\n" +
            "\tsource_user_id, target_user_id, type)\n" +
            "\tVALUES ( 18, 19, 'like');\n" +
            "\t\n" +
            "\tINSERT INTO public.interactions(\n" +
            "\tsource_user_id, target_user_id, type)\n" +
            "\tVALUES ( 19, 18, 'like');\n" +
            "\t\n" +
            "----------------------------------------------------------------------------------------------------\n" +
            "\n" +
            "--Reports\n" +
            "\n" +
            "\tINSERT INTO public.reports(\n" +
            "\tsource_user_id, target_user_id, reason, created_at)\n" +
            "\tVALUES (1, 17, 'racist bio', '2021-04-08 13:48:27.110024');\n" +
            "\t\n" +
            "\n" +
            "----------------------------------------------------------------------------------------------------\n" +
            "\n" +
            "--Blocks\n" +
            "\n" +
            "\tINSERT INTO public.blocks(\n" +
            "\tsource_user_id, target_user_id)\n" +
            "\tVALUES (19, 18);\n" +
            "\t\n" +
            "----------------------------------------------------------------------------------------------------\n" +
            "\n" +
            "--Bans\n" +
            "\n" +
            "\tINSERT INTO public.bans(\n" +
            "\tmoderator_id, user_id, reason, expiry_date)\n" +
            "\tVALUES (1, 17, 'racist bio', '2021-09-09 13:48:27.110024');\n" +
            "\t\n" +
            "\tINSERT INTO public.bans(\n" +
            "\tmoderator_id, user_id, reason, expiry_date)\n" +
            "\tVALUES (1, 12, 'racist bio_2', '2021-09-09 13:48:27.110024');\n" +
            "\t\n" +
            "\tINSERT INTO public.bans(\n" +
            "\tmoderator_id, user_id, reason, expiry_date)\n" +
            "\tVALUES (1, 12, 'racist bio_3', '2021-09-09 13:48:27.110024');\n" +
            "\t\n" +
            "----------------------------------------------------------------------------------------------------\n" +
            "\n" +
            "--Transactions\n" +
            "\n" +
            "\tINSERT INTO public.transactions(\n" +
            "\tuser_id, amount, created_at)\n" +
            "\tVALUES ( 16, 19.99, '2021-04-09 13:48:27.110024');\n" +
            "\t\n" +
            "\tINSERT INTO public.transactions(\n" +
            "\tuser_id, amount, created_at)\n" +
            "\tVALUES ( 15, 39.98, '2021-04-07 13:48:27.110024');\n" +
            "\t\n" +
            "\tINSERT INTO public.transactions(\n" +
            "\tuser_id, amount, created_at)\n" +
            "\tVALUES ( 11, 59.97, '2021-04-01 13:48:27.110024');\n" +
            "\t\n" +
            "\tINSERT INTO public.transactions(\n" +
            "\tuser_id, amount, created_at)\n" +
            "\tVALUES ( 12, 119.94, '2021-03-09 13:48:27.110024');\n" +
            "COMMIT;\n" ;
}
