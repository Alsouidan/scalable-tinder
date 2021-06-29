--
-- PostgreSQL database dump
--

-- Dumped from database version 13.2
-- Dumped by pg_dump version 13.2

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: banData; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public."banData" AS (
	id integer,
	moderator_id integer,
	user_id integer,
	reason character varying,
	created_at timestamp without time zone,
	expiry_date date
);


ALTER TYPE public."banData" OWNER TO postgres;

--
-- Name: blockData; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public."blockData" AS (
	in_source integer,
	in_target integer,
	in_created_at timestamp without time zone
);


ALTER TYPE public."blockData" OWNER TO postgres;

--
-- Name: blockdata; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.blockdata AS (
	source_user_id integer,
	target_user_id integer,
	created_at timestamp without time zone
);


ALTER TYPE public.blockdata OWNER TO postgres;

--
-- Name: interaction_type; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.interaction_type AS ENUM (
    'like',
    'dislike',
    'super_like'
);


ALTER TYPE public.interaction_type OWNER TO postgres;

--
-- Name: interactionData; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public."interactionData" AS (
	id integer,
	source_user_id integer,
	target_user_id integer,
	type public.interaction_type,
	created_at timestamp without time zone
);


ALTER TYPE public."interactionData" OWNER TO postgres;

--
-- Name: interactiondata; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.interactiondata AS (
	id integer,
	source_user_id integer,
	target_user_id integer,
	type public.interaction_type,
	created_at timestamp without time zone
);


ALTER TYPE public.interactiondata OWNER TO postgres;

--
-- Name: interestinfo; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.interestinfo AS (
	id integer,
	name character varying(200)
);


ALTER TYPE public.interestinfo OWNER TO postgres;

--
-- Name: moderatorauthenticationinfo; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.moderatorauthenticationinfo AS (
	id integer,
	email character varying,
	password character varying
);


ALTER TYPE public.moderatorauthenticationinfo OWNER TO postgres;

--
-- Name: moderatorinfo; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.moderatorinfo AS (
	id integer,
	email character varying(200)
);


ALTER TYPE public.moderatorinfo OWNER TO postgres;

--
-- Name: reportdata; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.reportdata AS (
	id integer,
	source_user_id integer,
	target_user_id integer,
	reason character varying(400),
	created_at timestamp without time zone
);


ALTER TYPE public.reportdata OWNER TO postgres;

--
-- Name: transaction; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.transaction AS (
	id integer,
	user_id integer,
	amount money
);


ALTER TYPE public.transaction OWNER TO postgres;

--
-- Name: transactionData; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public."transactionData" AS (
	in_id integer,
	in_user_id integer,
	in_amout money,
	in_created_at timestamp without time zone
);


ALTER TYPE public."transactionData" OWNER TO postgres;

--
-- Name: userauthenticationdata; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.userauthenticationdata AS (
	id integer,
	email character varying,
	password character varying
);


ALTER TYPE public.userauthenticationdata OWNER TO postgres;

--
-- Name: userprofile; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.userprofile AS (
	id integer,
	email character varying,
	first_name character varying,
	last_name character varying,
	is_premium boolean
);


ALTER TYPE public.userprofile OWNER TO postgres;

--
-- Name: userpublicdata; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.userpublicdata AS (
	id integer,
	email character varying,
	first_name character varying,
	last_name character varying
);


ALTER TYPE public.userpublicdata OWNER TO postgres;

--
-- Name: checkuserpremiummirrorinteraction(integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.checkuserpremiummirrorinteraction(source_id integer, target_id integer) RETURNS TABLE(source_user_id integer, target_user_id integer, is_source_premium boolean, is_target_premium boolean, type public.interaction_type)
    LANGUAGE plpgsql
    AS $$
BEGIN
	RETURN QUERY
	SELECT i.source_user_id,i.target_user_id,u.is_premium AS is_source_premium, u2.is_premium AS is_target_premium, i.type from public.interactions i
	INNER JOIN "users" u ON i.source_user_id = u.id
	INNER JOIN "users" u2 ON i.target_user_id = u2.id
	WHERE i.target_user_id = source_id AND i.source_user_id = target_id
	AND (i.type='like' or i.type='super_like')
	AND u.is_premium = true;
END;$$;


ALTER FUNCTION public.checkuserpremiummirrorinteraction(source_id integer, target_id integer) OWNER TO postgres;

--
-- Name: uspCheckIfUsersMatched(integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public."uspCheckIfUsersMatched"(first_user_id integer, second_user_id integer) RETURNS TABLE(id integer, source_user_id integer, target_user_id integer, type public.interaction_type, created_at timestamp without time zone)
    LANGUAGE plpgsql
    AS $$
 BEGIN
 	RETURN QUERY
 	SELECT * from public.interactions
 	WHERE (public.interactions.source_user_id = first_user_id AND
 		   public.interactions.target_user_id = second_user_id AND
 		   NOT public.interactions.type = 'dislike') OR
 		   (public.interactions.source_user_id = second_user_id AND
 		   public.interactions.target_user_id = first_user_id AND
 		   NOT public.interactions.type = 'dislike');
 END;$$;


ALTER FUNCTION public."uspCheckIfUsersMatched"(first_user_id integer, second_user_id integer) OWNER TO postgres;

--
-- Name: uspCreateBan(integer, integer, character varying, date); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public."uspCreateBan"(mod_id integer, user_id integer, in_reason character varying, in_expiry_date date) RETURNS public."banData"
    LANGUAGE plpgsql
    AS $$
DECLARE 
	temp_data "banData";
BEGIN
	INSERT INTO public.bans(moderator_id,user_id,reason,expiry_date) VALUES (mod_id,user_id,in_reason,in_expiry_date)
	RETURNING * INTO temp_data;
	RETURN temp_data;
END;
$$;


ALTER FUNCTION public."uspCreateBan"(mod_id integer, user_id integer, in_reason character varying, in_expiry_date date) OWNER TO postgres;

--
-- Name: uspDeleteBan(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public."uspDeleteBan"(in_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE 
	deleted_id INT;
BEGIN
	DELETE FROM public.bans 
	WHERE public.bans.id = in_id
	RETURNING public.bans.id INTO deleted_id;
	RETURN deleted_id;
END;
$$;


ALTER FUNCTION public."uspDeleteBan"(in_id integer) OWNER TO postgres;

--
-- Name: uspDeleteBlock(integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public."uspDeleteBlock"(in_source integer, in_target integer) RETURNS public."blockData"
    LANGUAGE plpgsql
    AS $$
DECLARE 
	temp_data "blockData";
BEGIN
	DELETE FROM public.blocks 
	WHERE public.blocks.source_user_id = in_source AND public.blocks.target_user_id = in_target
	RETURNING * INTO temp_data;
	RETURN temp_data;
END;
$$;


ALTER FUNCTION public."uspDeleteBlock"(in_source integer, in_target integer) OWNER TO postgres;

--
-- Name: uspDeleteInteraction(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public."uspDeleteInteraction"(interaction_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	deleted_id INT;
BEGIN
	DELETE FROM public.interactions as i
	WHERE i.id = interaction_id
	RETURNING i.id INTO deleted_id;
	RETURN deleted_id;
END;$$;


ALTER FUNCTION public."uspDeleteInteraction"(interaction_id integer) OWNER TO postgres;

--
-- Name: uspDeleteReport(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public."uspDeleteReport"(report_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	deleted_id INT;
BEGIN
	DELETE FROM public.reports as r
	WHERE r.id = report_id
	RETURNING r.id into deleted_id; 
	RETURN deleted_id;
END;$$;


ALTER FUNCTION public."uspDeleteReport"(report_id integer) OWNER TO postgres;

--
-- Name: uspDeleteTransaction(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public."uspDeleteTransaction"(in_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
    deleted_id INT;
BEGIN
	DELETE FROM public.transactions 
	WHERE public.transactions.id = in_id
	RETURNING public.transactions.id INTO deleted_id;
	RETURN deleted_id;
END;
$$;


ALTER FUNCTION public."uspDeleteTransaction"(in_id integer) OWNER TO postgres;

--
-- Name: uspReadAllBans(integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public."uspReadAllBans"(page integer, "limit" integer) RETURNS TABLE(id integer, moderator_id integer, user_id integer, reason character varying, created_at timestamp without time zone, expiry_date date)
    LANGUAGE plpgsql
    AS $$
BEGIN
	RETURN QUERY
	SELECT * From public.bans 
	LIMIT "limit" OFFSET page*"limit";
END;$$;


ALTER FUNCTION public."uspReadAllBans"(page integer, "limit" integer) OWNER TO postgres;

--
-- Name: uspReadAllBlocks(integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public."uspReadAllBlocks"(page integer, "limit" integer) RETURNS TABLE(source_user_id integer, target_user_id integer, created_at timestamp without time zone)
    LANGUAGE plpgsql
    AS $$
BEGIN
	RETURN QUERY
	SELECT blocks.source_user_id,blocks.target_user_id,blocks.created_at from public.blocks
	LIMIT "limit"
	OFFSET page*"limit";
END;
$$;


ALTER FUNCTION public."uspReadAllBlocks"(page integer, "limit" integer) OWNER TO postgres;

--
-- Name: uspReadAllTransactions(integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public."uspReadAllTransactions"(page integer, "limit" integer) RETURNS TABLE(id integer, user_id integer, amount money, created_at timestamp without time zone)
    LANGUAGE plpgsql
    AS $$
BEGIN
	RETURN QUERY
	SELECT transactions.id,transactions.user_id,transactions.amount,transactions.created_at from public.transactions
	LIMIT "limit"
	OFFSET page*"limit";
END;
$$;


ALTER FUNCTION public."uspReadAllTransactions"(page integer, "limit" integer) OWNER TO postgres;

--
-- Name: uspReadAllUserTransactions(integer, integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public."uspReadAllUserTransactions"(in_user_id integer, page integer, "limit" integer) RETURNS TABLE(id integer, user_id integer, amount money, created_at timestamp without time zone)
    LANGUAGE plpgsql
    AS $$
BEGIN
	RETURN QUERY
	SELECT transactions.id,transactions.user_id,transactions.amount,transactions.created_at from public.transactions
	WHERE transactions.user_id = in_user_id
	LIMIT "limit"
	OFFSET page*"limit";
	
END;
$$;


ALTER FUNCTION public."uspReadAllUserTransactions"(in_user_id integer, page integer, "limit" integer) OWNER TO postgres;

--
-- Name: uspReadBan(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public."uspReadBan"(in_id integer) RETURNS TABLE(id integer, moderator_id integer, user_id integer, reason character varying, expiry_date date)
    LANGUAGE plpgsql
    AS $$
BEGIN
	RETURN QUERY
	SELECT bans.id,bans.moderator_id,bans.user_id,bans.reason,bans.expiry_date from public.bans
	WHERE bans.id = in_id;
END;
$$;


ALTER FUNCTION public."uspReadBan"(in_id integer) OWNER TO postgres;

--
-- Name: uspReadInteraction(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public."uspReadInteraction"(interaction_id integer) RETURNS TABLE(id integer, source_user_id integer, target_user_id integer, type public.interaction_type, created_at timestamp without time zone)
    LANGUAGE plpgsql
    AS $$
BEGIN
	RETURN QUERY
	SELECT * from public.interactions
	WHERE public.interactions.id = interaction_id;
END;$$;


ALTER FUNCTION public."uspReadInteraction"(interaction_id integer) OWNER TO postgres;

--
-- Name: uspReadInteractions(integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public."uspReadInteractions"(page integer, "limit" integer) RETURNS TABLE(id integer, source_user_id integer, target_user_id integer, type public.interaction_type, created_at timestamp without time zone)
    LANGUAGE plpgsql
    AS $$
BEGIN
	RETURN QUERY
	SELECT * from public.interactions
	LIMIT "limit" OFFSET page*"limit";
END;$$;


ALTER FUNCTION public."uspReadInteractions"(page integer, "limit" integer) OWNER TO postgres;

--
-- Name: uspReadModeratorBans(integer, integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public."uspReadModeratorBans"(mod_id integer, page integer, "limit" integer) RETURNS TABLE(id integer, moderator_id integer, user_id integer, reason character varying, expiry_date date)
    LANGUAGE plpgsql
    AS $$
BEGIN
	RETURN QUERY
	SELECT bans.id,bans.moderator_id,bans.user_id,bans.reason,bans.expiry_date from public.bans
	WHERE bans.moderator_id = mod_id
	LIMIT "limit"
	OFFSET page*"limit";
END;
$$;


ALTER FUNCTION public."uspReadModeratorBans"(mod_id integer, page integer, "limit" integer) OWNER TO postgres;

--
-- Name: uspReadModeratorUserBans(integer, integer, integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public."uspReadModeratorUserBans"(mod_id integer, user_id integer, page integer, "limit" integer) RETURNS TABLE(id integer, moderator_id integer, usr_id integer, reason character varying, expiry_date date)
    LANGUAGE plpgsql
    AS $$
BEGIN
	RETURN QUERY
	SELECT bans.id,bans.moderator_id,bans.user_id,bans.reason,bans.expiry_date from public.bans
	WHERE public.bans.moderator_id = mod_id AND public.bans.user_id = "uspReadModeratorUserBans".user_id
	LIMIT "limit"
	OFFSET page*"limit";
END;
$$;


ALTER FUNCTION public."uspReadModeratorUserBans"(mod_id integer, user_id integer, page integer, "limit" integer) OWNER TO postgres;

--
-- Name: uspReadSourceBlocks(integer, integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public."uspReadSourceBlocks"(in_source_user integer, page integer, "limit" integer) RETURNS TABLE(source_user_id integer, target_user_id integer, created_at timestamp without time zone)
    LANGUAGE plpgsql
    AS $$
BEGIN
	RETURN QUERY
	SELECT blocks.source_user_id,blocks.target_user_id,blocks.created_at from public.blocks
	WHERE blocks.source_user_id = in_source_user
	LIMIT "limit"
	OFFSET page*"limit";
END;
$$;


ALTER FUNCTION public."uspReadSourceBlocks"(in_source_user integer, page integer, "limit" integer) OWNER TO postgres;

--
-- Name: uspReadTargetBlocks(integer, integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public."uspReadTargetBlocks"(in_target_user integer, page integer, "limit" integer) RETURNS TABLE(source_user_id integer, target_user_id integer, created_at timestamp without time zone)
    LANGUAGE plpgsql
    AS $$
BEGIN
	RETURN QUERY
	SELECT blocks.source_user_id,blocks.target_user_id,blocks.created_at from public.blocks
	WHERE blocks.target_user_id = in_target_user
	LIMIT "limit"
	OFFSET page*"limit";
END;
$$;


ALTER FUNCTION public."uspReadTargetBlocks"(in_target_user integer, page integer, "limit" integer) OWNER TO postgres;

--
-- Name: uspReadTransaction(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public."uspReadTransaction"(in_id integer) RETURNS TABLE(id integer, user_id integer, amount money, created_at timestamp without time zone)
    LANGUAGE plpgsql
    AS $$
BEGIN
	RETURN QUERY
	SELECT transactions.id,transactions.user_id,transactions.amount,transactions.created_at from public.transactions
	WHERE transactions.id = in_id;
END;
$$;


ALTER FUNCTION public."uspReadTransaction"(in_id integer) OWNER TO postgres;

--
-- Name: uspReadUserBans(integer, integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public."uspReadUserBans"(user_id integer, page integer, "limit" integer) RETURNS TABLE(id integer, moderator_id integer, usr_id integer, reason character varying, expiry_date date)
    LANGUAGE plpgsql
    AS $$
BEGIN
	RETURN QUERY
	SELECT bans.id,bans.moderator_id,bans.user_id,bans.reason,bans.expiry_date from public.bans
	WHERE public.bans.user_id = "uspReadUserBans".user_id
	LIMIT "limit"
	OFFSET page*"limit";
END;
$$;


ALTER FUNCTION public."uspReadUserBans"(user_id integer, page integer, "limit" integer) OWNER TO postgres;

--
-- Name: uspReadUserSourceInteractions(integer, integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public."uspReadUserSourceInteractions"(source_id integer, page integer, "limit" integer) RETURNS TABLE(id integer, source_user_id integer, target_user_id integer, type public.interaction_type, created_at timestamp without time zone)
    LANGUAGE plpgsql
    AS $$
BEGIN
	RETURN QUERY
	SELECT * from public.interactions
	WHERE public.interactions.source_user_id=source_id
	LIMIT "limit" OFFSET page*"limit";
END;$$;


ALTER FUNCTION public."uspReadUserSourceInteractions"(source_id integer, page integer, "limit" integer) OWNER TO postgres;

--
-- Name: uspReadUserTargetInteractions(integer, integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public."uspReadUserTargetInteractions"(target_id integer, page integer, "limit" integer) RETURNS TABLE(id integer, source_user_id integer, target_user_id integer, type public.interaction_type, created_at timestamp without time zone)
    LANGUAGE plpgsql
    AS $$
BEGIN
	RETURN QUERY
	SELECT * from public.interactions
	WHERE public.interactions.target_user_id=target_id
	LIMIT "limit" OFFSET page*"limit";
END;$$;


ALTER FUNCTION public."uspReadUserTargetInteractions"(target_id integer, page integer, "limit" integer) OWNER TO postgres;

--
-- Name: uspSeeMatchesChronological(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public."uspSeeMatchesChronological"(user_id integer) RETURNS TABLE(int_1_id integer, int_2_id integer, int_1_type public.interaction_type, int_2_type public.interaction_type, user_1_id integer, user_2_id integer, created_at timestamp without time zone)
    LANGUAGE plpgsql
    AS $$
BEGIN
	RETURN QUERY
	SELECT i1.id AS "interaction_1_id",i2.id AS "interaction_2_id",i1.type AS "interaction_1_type", 
	i2.type AS "interaction_2_type",i1.source_user_id AS "user_1_id", i1.target_user_id AS "user_2_id",
	(SELECT Max(v)FROM (VALUES (i2.created_at), (i1.created_at) ) AS value(v)) AS "created_at"
	FROM interactions as i1,interactions as i2
	WHERE i1.source_user_id=i2.target_user_id AND i1.target_user_id=i2.source_user_id 
	AND i1.target_user_id=user_id AND ((i1.type='like' or i1.type='super_like') AND (i2.type='like' or i2.type='super_like'))
	ORDER BY created_at DESC;
END;$$;


ALTER FUNCTION public."uspSeeMatchesChronological"(user_id integer) OWNER TO postgres;

--
-- Name: uspUpdateBan(integer, integer, integer, character varying, date); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public."uspUpdateBan"(in_id integer, mod_id integer, user_id integer, in_reason character varying, in_expiry_date date) RETURNS public."banData"
    LANGUAGE plpgsql
    AS $$
DECLARE 
	temp_data "banData";
BEGIN
	UPDATE public.bans SET 
	reason = COALESCE(in_reason,bans.reason),
	expiry_date = COALESCE(in_expiry_date,bans.expiry_date),
    user_id = COALESCE(user_id,bans.user_id),
 	moderator_id = COALESCE(mod_id,bans.moderator_id)
	WHERE public.bans.id = in_id
	RETURNING * INTO temp_data;
	RETURN temp_data;
END;
$$;


ALTER FUNCTION public."uspUpdateBan"(in_id integer, mod_id integer, user_id integer, in_reason character varying, in_expiry_date date) OWNER TO postgres;

--
-- Name: uspUpdateInteraction(integer, integer, integer, public.interaction_type); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public."uspUpdateInteraction"(int_id integer, source_id integer DEFAULT NULL::integer, target_id integer DEFAULT NULL::integer, int_type public.interaction_type DEFAULT NULL::public.interaction_type) RETURNS public.interactiondata
    LANGUAGE plpgsql
    AS $$
DECLARE updated_interaction interactionData;
BEGIN
    UPDATE interactions
       SET source_user_id      = COALESCE(source_id, interactions.source_user_id),
           target_user_id      = COALESCE(target_id, interactions.target_user_id),
           "type"              = COALESCE(int_type, interactions.type)
     WHERE interactions.id = int_id
     RETURNING * INTO updated_interaction;
	 RETURN updated_interaction;
END;$$;


ALTER FUNCTION public."uspUpdateInteraction"(int_id integer, source_id integer, target_id integer, int_type public.interaction_type) OWNER TO postgres;

--
-- Name: uspUpdateReport(integer, character varying); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public."uspUpdateReport"(report_id integer, updated_reason character varying DEFAULT NULL::character varying) RETURNS public.reportdata
    LANGUAGE plpgsql
    AS $$
DECLARE updated_report reportData;
BEGIN
    UPDATE reports
       SET reason = COALESCE(updated_reason, reports.reason)
     WHERE reports.id = report_id
	 RETURNING * INTO updated_report;
     RETURN updated_report;
END;$$;


ALTER FUNCTION public."uspUpdateReport"(report_id integer, updated_reason character varying) OWNER TO postgres;

--
-- Name: uspUpdateTransaction(integer, integer, money); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public."uspUpdateTransaction"(in_id integer, in_user_id integer, in_amout money) RETURNS public."transactionData"
    LANGUAGE plpgsql
    AS $$
DECLARE 
	temp_data "transactionData";
BEGIN
	UPDATE public.transactions SET 
	amount = COALESCE(in_amout,transactions.amount),
	user_id = COALESCE(in_user_id,transactions.user_id)
	WHERE public.transactions.id = in_id
	RETURNING * INTO temp_data;
	RETURN temp_data;
END;
$$;


ALTER FUNCTION public."uspUpdateTransaction"(in_id integer, in_user_id integer, in_amout money) OWNER TO postgres;

--
-- Name: uspblockuser(integer, integer, timestamp without time zone); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.uspblockuser(_source_user_id integer, _target_user_id integer, _created_at timestamp without time zone) RETURNS public.blockdata
    LANGUAGE plpgsql
    AS $$
DECLARE
	new_block_data blockData;
BEGIN
	INSERT INTO public.blocks(source_user_id, target_user_id, created_at) VALUES (_source_user_id, _target_user_id, _created_at)
	RETURNING * INTO new_block_data;
	RETURN new_block_data;
END;
$$;


ALTER FUNCTION public.uspblockuser(_source_user_id integer, _target_user_id integer, _created_at timestamp without time zone) OWNER TO postgres;

--
-- Name: uspcreateinteraction(integer, integer, public.interaction_type); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.uspcreateinteraction(source_user_id integer, target_user_id integer, _type public.interaction_type) RETURNS public."interactionData"
    LANGUAGE plpgsql
    AS $$
	declare
		interaction_data "interactionData";	
	begin
		insert into public.interactions(source_user_id,target_user_id,"type") 
		values (source_user_id,target_user_id,_type)	
	returning * into interaction_data;
	return interaction_data;
	commit;
	end; $$;


ALTER FUNCTION public.uspcreateinteraction(source_user_id integer, target_user_id integer, _type public.interaction_type) OWNER TO postgres;

--
-- Name: uspcreateinterest(character varying); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.uspcreateinterest(_name character varying) RETURNS public.interestinfo
    LANGUAGE plpgsql
    AS $$
DECLARE new_interest interestInfo;
BEGIN
    INSERT INTO public.interests("name")
    VALUES (_name)
    RETURNING * INTO new_interest;
    RETURN new_interest;
END; $$;


ALTER FUNCTION public.uspcreateinterest(_name character varying) OWNER TO postgres;

--
-- Name: uspcreatetransaction(integer, money); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.uspcreatetransaction(userid integer, value money) RETURNS public.transaction
    LANGUAGE plpgsql
    AS $$
DECLARE
    new_transaction Transaction;
BEGIN
INSERT INTO transactions (user_id, amount) VALUES (userid,value)
RETURNING id, user_id, amount INTO new_transaction;
UPDATE users
SET is_premium = true
WHERE id=userid;
RETURN new_transaction;
END$$;


ALTER FUNCTION public.uspcreatetransaction(userid integer, value money) OWNER TO postgres;

--
-- Name: uspdeleteinterest(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.uspdeleteinterest(_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
    deleted_id INT;
BEGIN
	DELETE FROM public.interests as i WHERE i.id = _id
	RETURNING i.id INTO deleted_id;
	RETURN deleted_id;
END;$$;


ALTER FUNCTION public.uspdeleteinterest(_id integer) OWNER TO postgres;

--
-- Name: uspdeletemod(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.uspdeletemod(_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
    deleted_id INT;
BEGIN
	DELETE FROM public.moderators as m WHERE m.id = _id
	RETURNING m.id INTO deleted_id;
	RETURN deleted_id;
END;$$;


ALTER FUNCTION public.uspdeletemod(_id integer) OWNER TO postgres;

--
-- Name: uspdeleteprofile(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.uspdeleteprofile(_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
    deleted_id INT;
BEGIN
	DELETE FROM public."users" as u WHERE u.id = _id
	RETURNING u.id INTO deleted_id;
	RETURN deleted_id;
END;$$;


ALTER FUNCTION public.uspdeleteprofile(_id integer) OWNER TO postgres;

--
-- Name: uspeditaccountdata(integer, character varying, character varying, character varying); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.uspeditaccountdata(_id integer, _email character varying, _first_name character varying, _last_name character varying) RETURNS public.userpublicdata
    LANGUAGE plpgsql
    AS $$
DECLARE
    updated_user userPublicData;
BEGIN
	UPDATE public.users AS u SET
	 email = COALESCE(_email, u.email),
	 first_name = COALESCE(_first_name, u.first_name),
	 last_name = COALESCE(_last_name, u.last_name)
	 WHERE u.id = _id
	 RETURNING id, email, first_name, last_name INTO updated_user;
	 RETURN updated_user;
END;$$;


ALTER FUNCTION public.uspeditaccountdata(_id integer, _email character varying, _first_name character varying, _last_name character varying) OWNER TO postgres;

--
-- Name: uspeditinterest(integer, character varying); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.uspeditinterest(_id integer, _name character varying) RETURNS public.interestinfo
    LANGUAGE plpgsql
    AS $$
DECLARE
    updated_interest interestInfo;
BEGIN
	UPDATE public.interests AS i SET
	 "name" = COALESCE(_name, i."name")
	 WHERE i.id = _id
	 RETURNING id, "name" INTO updated_interest;
	 RETURN updated_interest;
END;$$;


ALTER FUNCTION public.uspeditinterest(_id integer, _name character varying) OWNER TO postgres;

--
-- Name: uspeditmod(integer, character varying); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.uspeditmod(_id integer, _email character varying) RETURNS public.moderatorinfo
    LANGUAGE plpgsql
    AS $$
DECLARE
    updated_moderator moderatorInfo;
BEGIN
	UPDATE public.moderators AS m SET
	 email = COALESCE(_email, m.email)
	 WHERE m.id = _id
	 RETURNING id, email INTO updated_moderator;
	 RETURN updated_moderator;
END;$$;


ALTER FUNCTION public.uspeditmod(_id integer, _email character varying) OWNER TO postgres;

--
-- Name: uspeditmodpassword(integer, character varying, character varying); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.uspeditmodpassword(_id integer, old_password character varying, new_password character varying) RETURNS public.moderatorinfo
    LANGUAGE plpgsql
    AS $$
DECLARE
    updated_moderator moderatorInfo;
BEGIN
	UPDATE public.moderators AS m SET
	 password = new_password
	 WHERE m.id = _id AND m.password = old_password
	 RETURNING moderatorInfo INTO updated_moderator;
	 RETURN updated_moderator;
END;$$;


ALTER FUNCTION public.uspeditmodpassword(_id integer, old_password character varying, new_password character varying) OWNER TO postgres;

--
-- Name: uspeditpassword(integer, character varying, character varying); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.uspeditpassword(_id integer, old_password character varying, new_password character varying) RETURNS public.userpublicdata
    LANGUAGE plpgsql
    AS $$
DECLARE
    updated_user userPublicData;
BEGIN
	UPDATE public.users AS u SET
	 password = new_password
	 WHERE u.id = _id AND u.password = old_password
	 RETURNING id, email, first_name, last_name INTO updated_user;
	 RETURN updated_user;
END;$$;


ALTER FUNCTION public.uspeditpassword(_id integer, old_password character varying, new_password character varying) OWNER TO postgres;

--
-- Name: usplogin(character varying); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.usplogin(_email character varying) RETURNS public.userauthenticationdata
    LANGUAGE plpgsql
    AS $$
DECLARE
    authenticated_user userAuthenticationData;
BEGIN
	SELECT id, email, password From public."users" AS u WHERE u.email = _email
	INTO authenticated_user;
	RETURN authenticated_user;
END;$$;


ALTER FUNCTION public.usplogin(_email character varying) OWNER TO postgres;

--
-- Name: uspmodlogin(character varying); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.uspmodlogin(_email character varying) RETURNS public.moderatorauthenticationinfo
    LANGUAGE plpgsql
    AS $$
DECLARE
    authenticated_moderator moderatorAuthenticationInfo;
BEGIN
	SELECT "id", email, password From public.moderators AS m WHERE m.email = _email
	INTO authenticated_moderator;
	RETURN authenticated_moderator;
END;$$;


ALTER FUNCTION public.uspmodlogin(_email character varying) OWNER TO postgres;

--
-- Name: uspmodsignup(character varying, character varying); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.uspmodsignup(_email character varying, _password character varying) RETURNS public.moderatorinfo
    LANGUAGE plpgsql
    AS $$
DECLARE new_mod moderatorInfo;
BEGIN
    INSERT INTO public.moderators(email,"password")
    VALUES (_email, _password)
    RETURNING * INTO new_mod;
    RETURN new_mod;
END; $$;


ALTER FUNCTION public.uspmodsignup(_email character varying, _password character varying) OWNER TO postgres;

--
-- Name: uspreportuser(integer, integer, character varying, timestamp without time zone); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.uspreportuser(_source_user_id integer, _target_user_id integer, _reason character varying, _created_at timestamp without time zone) RETURNS public.reportdata
    LANGUAGE plpgsql
    AS $$
DECLARE
	new_report_data reportData;
BEGIN
	INSERT INTO public.reports(source_user_id, target_user_id, reason, created_at) VALUES (_source_user_id, _target_user_id, _reason, _created_at)
	RETURNING * INTO new_report_data;
	RETURN new_report_data;
END;
$$;


ALTER FUNCTION public.uspreportuser(_source_user_id integer, _target_user_id integer, _reason character varying, _created_at timestamp without time zone) OWNER TO postgres;

--
-- Name: uspsetuserpremium(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.uspsetuserpremium(_id integer) RETURNS public.userprofile
    LANGUAGE plpgsql
    AS $$
DECLARE
    updated_user userProfile;
BEGIN
	UPDATE public.users AS u SET
	 is_premium = true
	 WHERE u.id = _id
	 RETURNING id, email, first_name, last_name,is_premium INTO updated_user;
	 RETURN updated_user;
END;$$;


ALTER FUNCTION public.uspsetuserpremium(_id integer) OWNER TO postgres;

--
-- Name: uspsignup(character varying, character varying, character varying, character varying); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.uspsignup(_email character varying, _password character varying, _first_name character varying, _last_name character varying) RETURNS public.userpublicdata
    LANGUAGE plpgsql
    AS $$
DECLARE
   new_user userPublicData;
BEGIN
	INSERT INTO public.users(email, "password", first_name, last_name) VALUES
	(_email,"_password", _first_name, _last_name)
	 RETURNING id, email, first_name, last_name INTO new_user;
	 RETURN new_user;
END;$$;


ALTER FUNCTION public.uspsignup(_email character varying, _password character varying, _first_name character varying, _last_name character varying) OWNER TO postgres;

--
-- Name: uspviewinterests(integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.uspviewinterests(page integer, "limit" integer) RETURNS TABLE(id integer, name character varying)
    LANGUAGE plpgsql
    AS $$
BEGIN
	RETURN QUERY
	SELECT interests.id, interests.name::varchar(200) From public.interests 
	LIMIT "limit" OFFSET page*"limit";
END;$$;


ALTER FUNCTION public.uspviewinterests(page integer, "limit" integer) OWNER TO postgres;

--
-- Name: uspviewmod(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.uspviewmod(_id integer) RETURNS public.moderatorinfo
    LANGUAGE plpgsql
    AS $$
DECLARE
    moderator_profile moderatorInfo;
BEGIN
	SELECT id, email From public.moderators AS m 
	WHERE id = _id 
	INTO moderator_profile;
	RETURN moderator_profile;
END;$$;


ALTER FUNCTION public.uspviewmod(_id integer) OWNER TO postgres;

--
-- Name: uspviewprofile(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.uspviewprofile(_id integer) RETURNS public.userprofile
    LANGUAGE plpgsql
    AS $$
DECLARE
    user_profile userProfile;
BEGIN
	SELECT id, email, first_name, last_name,is_premium From public."users" AS u
	WHERE id = _id
	INTO user_profile;
	RETURN user_profile;
END;$$;


ALTER FUNCTION public.uspviewprofile(_id integer) OWNER TO postgres;

--
-- Name: uspviewreported(integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.uspviewreported(page integer, "limit" integer) RETURNS TABLE(id integer, target_user_id integer, reason character varying)
    LANGUAGE plpgsql
    AS $$
DECLARE
BEGIN
	RETURN QUERY
    SELECT reports.id, reports.target_user_id, reports.reason From public.reports 
	ORDER BY created_at LIMIT "limit" OFFSET page*"limit";
END;$$;


ALTER FUNCTION public.uspviewreported(page integer, "limit" integer) OWNER TO postgres;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: bans; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.bans (
    id integer NOT NULL,
    moderator_id integer NOT NULL,
    user_id integer NOT NULL,
    reason character varying(400),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    expiry_date date NOT NULL
);


ALTER TABLE public.bans OWNER TO postgres;

--
-- Name: bans_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

ALTER TABLE public.bans ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.bans_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: blocks; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.blocks (
    source_user_id integer NOT NULL,
    target_user_id integer NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.blocks OWNER TO postgres;

--
-- Name: interactions; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.interactions (
    id integer NOT NULL,
    source_user_id integer NOT NULL,
    target_user_id integer NOT NULL,
    type public.interaction_type NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.interactions OWNER TO postgres;

--
-- Name: interactions_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

ALTER TABLE public.interactions ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.interactions_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: interests; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.interests (
    id integer NOT NULL,
    name character(200) NOT NULL
);


ALTER TABLE public.interests OWNER TO postgres;

--
-- Name: interests_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

ALTER TABLE public.interests ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.interests_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: moderators; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.moderators (
    id integer NOT NULL,
    email character varying(200) NOT NULL,
    password character varying(200) NOT NULL,
    CONSTRAINT proper_email CHECK (((email)::text ~* '^[A-Za-z0-9._%-]+@[A-Za-z0-9.-]+[.][A-Za-z]+$'::text))
);


ALTER TABLE public.moderators OWNER TO postgres;

--
-- Name: moderators_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

ALTER TABLE public.moderators ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.moderators_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: reports; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.reports (
    id integer NOT NULL,
    source_user_id integer NOT NULL,
    target_user_id integer NOT NULL,
    reason character varying(400),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.reports OWNER TO postgres;

--
-- Name: reports_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

ALTER TABLE public.reports ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.reports_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: transactions; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.transactions (
    id integer NOT NULL,
    user_id integer,
    amount money NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.transactions OWNER TO postgres;

--
-- Name: transactions_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

ALTER TABLE public.transactions ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.transactions_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users (
    id integer NOT NULL,
    email character varying(200) NOT NULL,
    password character varying(200) NOT NULL,
    is_banned boolean DEFAULT false,
    is_premium boolean DEFAULT false,
    credit_card_token character(200),
    first_name character varying(200) NOT NULL,
    last_name character varying(200) NOT NULL,
    CONSTRAINT proper_email CHECK (((email)::text ~* '^[A-Za-z0-9._%-]+@[A-Za-z0-9.-]+[.][A-Za-z]+$'::text))
);


ALTER TABLE public.users OWNER TO postgres;

--
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

ALTER TABLE public.users ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Data for Name: bans; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.bans (id, moderator_id, user_id, reason, created_at, expiry_date) FROM stdin;
2	1	12	racist bio_2	2021-06-28 17:33:54.539135	2021-09-09
3	1	12	racist bio_3	2021-06-28 17:33:54.539135	2021-09-09
4	1	6	Ban reason	2021-06-28 17:33:58.069618	2022-09-09
\.


--
-- Data for Name: blocks; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.blocks (source_user_id, target_user_id, created_at) FROM stdin;
3	16	2021-06-28 17:34:05
\.


--
-- Data for Name: interactions; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.interactions (id, source_user_id, target_user_id, type, created_at) FROM stdin;
2	16	12	dislike	2021-06-28 17:33:54.539135
4	16	14	dislike	2021-06-28 17:33:54.539135
5	16	15	like	2021-06-28 17:33:54.539135
6	1	16	like	2021-06-28 17:33:54.539135
7	1	8	dislike	2021-06-28 17:33:54.539135
8	8	12	dislike	2021-06-28 17:33:54.539135
9	11	15	super_like	2021-06-28 17:33:54.539135
10	15	11	super_like	2021-06-28 17:33:54.539135
11	12	16	like	2021-06-28 17:33:54.539135
12	18	19	like	2021-06-28 17:33:54.539135
13	19	18	like	2021-06-28 17:33:54.539135
3	16	13	dislike	2021-06-28 17:33:54.539135
14	19	18	like	2021-06-28 17:34:08.171775
\.


--
-- Data for Name: interests; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.interests (id, name) FROM stdin;
1	Surfing                                                                                                                                                                                                 
2	Volunteering                                                                                                                                                                                            
3	Tea                                                                                                                                                                                                     
4	Politics                                                                                                                                                                                                
5	Art                                                                                                                                                                                                     
6	Instagram                                                                                                                                                                                               
7	Spirituality                                                                                                                                                                                            
8	Dog Lover                                                                                                                                                                                               
9	DIY                                                                                                                                                                                                     
10	Sports                                                                                                                                                                                                  
11	Cycling                                                                                                                                                                                                 
12	Foodie                                                                                                                                                                                                  
13	Astrology                                                                                                                                                                                               
14	Netflix                                                                                                                                                                                                 
17	Test Interest                                                                                                                                                                                           
15	updatedName                                                                                                                                                                                             
\.


--
-- Data for Name: moderators; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.moderators (id, email, password) FROM stdin;
1	hussein.badr@gmail.com	$2a$12$W5/J6mREYFuQoVxPUEgkUOt050wgiu/i.uKKEjLp6d3eb/83UprMa
2	youssef.sameh@gmail.com	$2a$12$W5/J6mREYFuQoVxPUEgkUOt050wgiu/i.uKKEjLp6d3eb/83UprMa
3	test@gmail.com	$2a$12$qA8GPz.rnNMGUjEJHb9xdO0aaRPYZ7vsXQeOMufIZYjX8rYmAhCvO
\.


--
-- Data for Name: reports; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.reports (id, source_user_id, target_user_id, reason, created_at) FROM stdin;
2	19	18	Spamming	2021-06-28 17:34:03
\.


--
-- Data for Name: transactions; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.transactions (id, user_id, amount, created_at) FROM stdin;
3	11	$59.97	2021-04-01 13:48:27.110024
4	12	$119.94	2021-03-09 13:48:27.110024
6	1	$500.00	2021-06-28 17:34:00.945346
2	15	$510.00	2021-04-07 13:48:27.110024
\.


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.users (id, email, password, is_banned, is_premium, credit_card_token, first_name, last_name) FROM stdin;
2	doja.cat@gmail.com	$2a$12$W5/J6mREYFuQoVxPUEgkUOt050wgiu/i.uKKEjLp6d3eb/83UprMa	f	f	\N	Doja	Cat
3	billie.eilish@gmail.com	$2a$12$W5/J6mREYFuQoVxPUEgkUOt050wgiu/i.uKKEjLp6d3eb/83UprMa	f	f	\N	Billie	Eilish
4	megan.stalyon@gmail.com	$2a$12$W5/J6mREYFuQoVxPUEgkUOt050wgiu/i.uKKEjLp6d3eb/83UprMa	f	f	\N	Megan	The Stalyon
5	selena.gomez@gmail.com	$2a$12$W5/J6mREYFuQoVxPUEgkUOt050wgiu/i.uKKEjLp6d3eb/83UprMa	f	f	\N	Selena	Gomez
6	justin.bieber@gmail.com	$2a$12$W5/J6mREYFuQoVxPUEgkUOt050wgiu/i.uKKEjLp6d3eb/83UprMa	f	f	\N	Justin	Bieber
7	post.malone@gmail.com	$2a$12$W5/J6mREYFuQoVxPUEgkUOt050wgiu/i.uKKEjLp6d3eb/83UprMa	f	f	\N	Post	Malone
8	taylor.swift@gmail.com	$2a$12$W5/J6mREYFuQoVxPUEgkUOt050wgiu/i.uKKEjLp6d3eb/83UprMa	f	t	5186975364203198                                                                                                                                                                                        	Taylor	Swift
9	dua.lippa@gmail.com	$2a$12$W5/J6mREYFuQoVxPUEgkUOt050wgiu/i.uKKEjLp6d3eb/83UprMa	f	f	\N	Dua	Lippa
10	jennifer.lopez@gmail.com	$2a$12$W5/J6mREYFuQoVxPUEgkUOt050wgiu/i.uKKEjLp6d3eb/83UprMa	f	f	\N	Jennifer	Lopez
11	mariah.carey@gmail.com	$2a$12$W5/J6mREYFuQoVxPUEgkUOt050wgiu/i.uKKEjLp6d3eb/83UprMa	f	t	5192086875353209                                                                                                                                                                                        	Mariah	Carey
12	drake@gmail.com	$2a$12$W5/J6mREYFuQoVxPUEgkUOt050wgiu/i.uKKEjLp6d3eb/83UprMa	f	t	5186425319208694                                                                                                                                                                                        	Aubrey	Drake
13	dj.khaled@gmail.com	$2a$12$W5/J6mREYFuQoVxPUEgkUOt050wgiu/i.uKKEjLp6d3eb/83UprMa	f	f	\N	DJ	Khaled
14	travis.scott@gmail.com	$2a$12$W5/J6mREYFuQoVxPUEgkUOt050wgiu/i.uKKEjLp6d3eb/83UprMa	f	f	\N	Travis	Scott
15	jack.harlow@gmail.com	$2a$12$W5/J6mREYFuQoVxPUEgkUOt050wgiu/i.uKKEjLp6d3eb/83UprMa	f	f	\N	Jack	Harlow
16	kanye.west@gmail.com	$2a$12$W5/J6mREYFuQoVxPUEgkUOt050wgiu/i.uKKEjLp6d3eb/83UprMa	f	t	5192086975364205                                                                                                                                                                                        	Kanye	West
17	donald.trump@gmail.com	$2a$12$W5/J6mREYFuQoVxPUEgkUOt050wgiu/i.uKKEjLp6d3eb/83UprMa	f	f	\N	Donald	Trump
18	liam.hemsworth@gmail.com	$2a$12$W5/J6mREYFuQoVxPUEgkUOt050wgiu/i.uKKEjLp6d3eb/83UprMa	f	f	\N	Liam	Hemsworth
19	miley.cyrus@gmail.com	$2a$12$W5/J6mREYFuQoVxPUEgkUOt050wgiu/i.uKKEjLp6d3eb/83UprMa	f	f	\N	Miley	Cyrus
1	ariannagrande@gmail.com	$2a$12$W5/J6mREYFuQoVxPUEgkUOt050wgiu/i.uKKEjLp6d3eb/83UprMa	f	t	\N	Arianna	Grande
\.


--
-- Name: bans_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.bans_id_seq', 4, true);


--
-- Name: interactions_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.interactions_id_seq', 14, true);


--
-- Name: interests_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.interests_id_seq', 17, true);


--
-- Name: moderators_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.moderators_id_seq', 3, true);


--
-- Name: reports_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.reports_id_seq', 2, true);


--
-- Name: transactions_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.transactions_id_seq', 6, true);


--
-- Name: users_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.users_id_seq', 19, true);


--
-- Name: bans bans_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.bans
    ADD CONSTRAINT bans_pkey PRIMARY KEY (id);


--
-- Name: blocks blocks_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.blocks
    ADD CONSTRAINT blocks_pkey PRIMARY KEY (source_user_id, target_user_id);


--
-- Name: interactions interactions_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.interactions
    ADD CONSTRAINT interactions_pkey PRIMARY KEY (id);


--
-- Name: interests interests_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.interests
    ADD CONSTRAINT interests_pkey PRIMARY KEY (id);


--
-- Name: moderators moderators_email_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.moderators
    ADD CONSTRAINT moderators_email_key UNIQUE (email);


--
-- Name: moderators moderators_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.moderators
    ADD CONSTRAINT moderators_pkey PRIMARY KEY (id);


--
-- Name: reports reports_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.reports
    ADD CONSTRAINT reports_pkey PRIMARY KEY (id);


--
-- Name: transactions transactions_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.transactions
    ADD CONSTRAINT transactions_pkey PRIMARY KEY (id);


--
-- Name: users users_email_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_email_key UNIQUE (email);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: bans bans_moderator_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.bans
    ADD CONSTRAINT bans_moderator_id_fkey FOREIGN KEY (moderator_id) REFERENCES public.moderators(id);


--
-- Name: bans bans_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.bans
    ADD CONSTRAINT bans_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: blocks blocks_source_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.blocks
    ADD CONSTRAINT blocks_source_user_id_fkey FOREIGN KEY (source_user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: blocks blocks_target_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.blocks
    ADD CONSTRAINT blocks_target_user_id_fkey FOREIGN KEY (target_user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: interactions interactions_source_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.interactions
    ADD CONSTRAINT interactions_source_user_id_fkey FOREIGN KEY (source_user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: interactions interactions_target_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.interactions
    ADD CONSTRAINT interactions_target_user_id_fkey FOREIGN KEY (target_user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: reports reports_source_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.reports
    ADD CONSTRAINT reports_source_user_id_fkey FOREIGN KEY (source_user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: reports reports_target_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.reports
    ADD CONSTRAINT reports_target_user_id_fkey FOREIGN KEY (target_user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: transactions transactions_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.transactions
    ADD CONSTRAINT transactions_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- PostgreSQL database dump complete
--

